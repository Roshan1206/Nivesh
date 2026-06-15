-- =============================================================
-- Nivesh Bank — Transaction Service
-- Migration: V3__outbox.sql
-- Changes:
--   1. Add TRANSFER to txn.transaction_type enum
--   2. Rebuild txn.transaction_status enum (remove CREDIT_RETRY)
--   3. Remove credit_retry_count column from txn.transactions
--   4. Create txn.outbox_events table
--   5. Create txn.outbox_status enum
--   6. Create Postgres publication for Debezium CDC
--   7. Seed TRANSFER transaction_type_config
-- =============================================================

-- -------------------------------------------------------------
-- STEP 1: Add TRANSFER and RD_MATURITY to transaction_type enum
-- ALTER TYPE ADD VALUE cannot run inside a transaction in PG<13.
-- PG14+ allows it. We guard with DO + dynamic SQL just in case.
-- -------------------------------------------------------------
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_enum e
                              JOIN pg_type t ON t.oid = e.enumtypid
                              JOIN pg_namespace n ON n.oid = t.typnamespace
            WHERE n.nspname = 'txn'
              AND t.typname = 'transaction_type'
              AND e.enumlabel = 'TRANSFER'
        ) THEN
            ALTER TYPE txn.transaction_type ADD VALUE 'TRANSFER' AFTER 'STANDING_INSTRUCTION';
        END IF;
    END$$;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_enum e
                              JOIN pg_type t ON t.oid = e.enumtypid
                              JOIN pg_namespace n ON n.oid = t.typnamespace
            WHERE n.nspname = 'txn'
              AND t.typname = 'transaction_type'
              AND e.enumlabel = 'RD_MATURITY'
        ) THEN
            ALTER TYPE txn.transaction_type ADD VALUE 'RD_MATURITY' AFTER 'FD_MATURITY';
        END IF;
    END$$;

-- -------------------------------------------------------------
-- STEP 2: Rebuild transaction_status enum without CREDIT_RETRY
--
-- Strategy: avoid ALTER TYPE RENAME entirely.
-- Instead we:
--   a) add a temp TEXT shadow column
--   b) copy status values as text into it
--   c) drop the old enum column
--   d) create the new enum type under the original name
--      (only possible because the column no longer references it)
--   e) add the column back with the new enum type
--   f) populate from the shadow column with the value mapping
--   g) drop the shadow column
--
-- This avoids any cross-type operator comparison that causes
-- the "operator does not exist" error.
-- -------------------------------------------------------------

-- 2a. Drop the column default so we can freely alter the column
ALTER TABLE txn.transactions ALTER COLUMN status DROP DEFAULT;

-- 2b. Add a temporary text shadow column and copy current values
ALTER TABLE txn.transactions ADD COLUMN status_text TEXT;
UPDATE txn.transactions SET status_text = status::text;

-- 2c. Drop the status column entirely (releases the enum type)
ALTER TABLE txn.transactions DROP COLUMN status;

-- 2d. Drop the old enum type (now safe — nothing references it)
DROP TYPE txn.transaction_status;

-- 2e. Create the new clean enum under the same name
CREATE TYPE txn.transaction_status AS ENUM (
    'INITIATED',            -- Row written, outbox event queued, fraud not yet checked
    'FRAUD_CHECK',          -- Fraud Service scoring. Money has NOT moved yet.
    'PENDING',              -- Fraud passed. Debit outbox event published. Awaiting Account Service.
    'DEBIT_SUCCESS',        -- Account Service confirmed debit. Credit outbox event queued.
    'POSTED',               -- TERMINAL. Debit + Credit succeeded. JournalEntries written.
    'FAILED',               -- TERMINAL. Debit failed. No money moved. No compensation needed.
    'COMPENSATE_INITIATED', -- Credit failed. Compensation outbox event queued.
    'REVERSED',             -- TERMINAL. Compensation succeeded. Source account re-credited.
    'BLOCKED',              -- TERMINAL. Fraud flagged. No money moved. No compensation needed.
    'MANUAL_REVIEW'         -- TERMINAL. Compensation exhausted. Ops team must intervene.
    );

-- 2f. Re-add the status column with the new enum type
ALTER TABLE txn.transactions
    ADD COLUMN status txn.transaction_status NOT NULL DEFAULT 'INITIATED';

-- 2g. Populate from shadow column, mapping removed/changed values
UPDATE txn.transactions
SET status = CASE status_text
                 WHEN 'CREDIT_RETRY' THEN 'COMPENSATE_INITIATED'::txn.transaction_status
                 WHEN 'TRANSFER'     THEN 'INITIATED'::txn.transaction_status
                 ELSE status_text::txn.transaction_status
    END;

-- 2h. Drop the shadow column
ALTER TABLE txn.transactions DROP COLUMN status_text;

-- -------------------------------------------------------------
-- STEP 3: Remove credit_retry_count from transactions
-- -------------------------------------------------------------
ALTER TABLE txn.transactions DROP COLUMN IF EXISTS credit_retry_count;

-- Add last_outbox_event_id for tracing (optional debugging aid)
ALTER TABLE txn.transactions ADD COLUMN IF NOT EXISTS
    last_outbox_event_id UUID;

-- -------------------------------------------------------------
-- STEP 4: Create outbox_status enum
-- -------------------------------------------------------------
CREATE TYPE txn.outbox_status AS ENUM (
    'PENDING',    -- Written to DB. Debezium has not yet read it.
    'PUBLISHED',  -- Debezium confirmed publish to Kafka.
    'FAILED'      -- Debezium failed after max retries. Needs ops attention.
    );

-- -------------------------------------------------------------
-- STEP 5: Create outbox_events table
-- payload is TEXT not JSONB — Debezium reads WAL as raw text;
-- JSONB is stored as binary in WAL and requires re-serialization.
-- -------------------------------------------------------------
CREATE TABLE txn.outbox_events (
                                   event_id        UUID              NOT NULL DEFAULT gen_random_uuid(),
                                   aggregate_type  VARCHAR(50)       NOT NULL DEFAULT 'TRANSACTION',
                                   aggregate_id    VARCHAR(50)       NOT NULL,   -- referenceNumber → Kafka message key
                                   topic           VARCHAR(100)      NOT NULL,   -- exact Kafka topic name
                                   event_type      VARCHAR(60)       NOT NULL,   -- e.g. 'DEBIT_REQUESTED', 'CREDIT_REQUESTED'
                                   payload         TEXT              NOT NULL,   -- JSON string; never JSONB (WAL reason above)
                                   status          txn.outbox_status NOT NULL DEFAULT 'PENDING',
                                   created_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                                   published_at    TIMESTAMPTZ,                  -- set by Debezium SMT after successful publish

                                   CONSTRAINT pk_outbox_events PRIMARY KEY (event_id)
);

-- Index for monitoring: "how many PENDING events are older than 30s?"
CREATE INDEX idx_outbox_status_created ON txn.outbox_events (status, created_at)
    WHERE status = 'PENDING';

COMMENT ON TABLE txn.outbox_events IS
    'Transactional outbox. Rows written atomically with txn.transactions. '
        'Debezium reads WAL and publishes to Kafka, then deletes the row. '
        'payload is TEXT (not JSONB) to avoid WAL binary re-serialization.';

COMMENT ON COLUMN txn.outbox_events.aggregate_id IS
    'referenceNumber of the transaction. Used as Kafka message key — '
        'guarantees all events for one transaction land on the same partition (ordering).';

COMMENT ON COLUMN txn.outbox_events.topic IS
    'Kafka topic. Debezium EventRouter SMT reads this column to route. '
        'Convention: txn.{entity}.{action} e.g. txn.debit.requested';

-- -------------------------------------------------------------
-- STEP 6: Create Postgres publication for Debezium
-- Only outbox_events — publishing all tables floods WAL with noise.
-- -------------------------------------------------------------
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_publication WHERE pubname = 'nivesh_outbox_pub'
        ) THEN
            EXECUTE 'CREATE PUBLICATION nivesh_outbox_pub FOR TABLE txn.outbox_events';
        END IF;
    END$$;

-- -------------------------------------------------------------
-- STEP 7: Seed TRANSFER transaction_type_config
-- GL 900400 Reversal Clearing Account acts as in-transit bridge
-- for funds moving between two Nivesh accounts.
-- -------------------------------------------------------------
INSERT INTO txn.transaction_type_configs
(type_code, name, gl_account_id, max_limit_daily,
 charge_applicable, reversal_allowed, settlement_type,
 requires_beneficiary, is_active)
VALUES (
           'TRANSFER',
           'Internal Fund Transfer',
           (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '900400'),
           1000000.0000,   -- ₹10,00,000 daily; matches DEBIT limit (RBI IMPS cap reference)
           FALSE,          -- no fee on internal transfers
           TRUE,           -- transfers can be reversed (customer error scenario)
           'INSTANT',      -- internal transfers settle immediately
           TRUE,           -- destination account is mandatory
           TRUE
       )
ON CONFLICT (type_code) DO NOTHING;

-- =============================================================
-- VERIFICATION QUERIES (run manually after migration)
-- =============================================================
-- SELECT unnest(enum_range(NULL::txn.transaction_type))   AS type_value;
-- SELECT unnest(enum_range(NULL::txn.transaction_status)) AS status_value;
-- SELECT unnest(enum_range(NULL::txn.outbox_status))      AS outbox_status_value;
-- \d txn.outbox_events
-- SELECT tc.type_code, tc.name, ga.gl_code
-- FROM txn.transaction_type_configs tc
-- JOIN txn.gl_accounts ga ON ga.gl_account_id = tc.gl_account_id
-- WHERE tc.type_code = 'TRANSFER';
-- SELECT * FROM pg_publication WHERE pubname = 'nivesh_outbox_pub';
-- =============================================================
-- =============================================================
-- Nivesh Bank — Transaction Service
-- Migration: V1__create_transaction_service_tables.sql
-- =============================================================

CREATE SCHEMA IF NOT EXISTS txn;

-- =============================================================
-- ENUM TYPES
-- Defined once here. Referenced by every table that needs them.
-- WHY NATIVE ENUM vs VARCHAR+CHECK:
--   1. PostgreSQL rejects invalid values at the type level — not
--      just at constraint evaluation time.
--   2. Storage: enum = 4 bytes vs VARCHAR = variable + header.
--   3. Self-documenting — \dT in psql shows all valid values.
--   4. Adding a new value: ALTER TYPE ... ADD VALUE (no table lock).
--   5. Removing a value: requires migration — intentional friction
--      that forces you to think before deleting a valid state.
-- =============================================================

-- Transaction business classification (customer perspective).
-- NOT the same as dr_cr — that is accounting direction.
CREATE TYPE txn.transaction_type AS ENUM (
    'DEBIT',               -- Customer initiates outbound payment
    'CREDIT',              -- Money arrives into customer account
    'REVERSAL',            -- Technical undo of a POSTED transaction
    'FEE',                 -- Service charge levied by the bank
    'INTEREST',            -- Interest credited to savings/FD/RD
    'REFUND',              -- Goodwill/regulatory return to customer
    'STANDING_INSTRUCTION',-- Auto-debit from SI batch job
    'FD_MATURITY'          -- FD maturity payout
    );

-- State machine for a transaction row.
-- Terminal states: POSTED, FAILED, REVERSED, BLOCKED.
-- No transition out of a terminal state is allowed.
CREATE TYPE txn.transaction_status AS ENUM (
    'INITIATED',    -- Row written, Kafka event published, fraud not yet checked
    'FRAUD_CHECK',  -- Fraud Service scoring. Money has NOT moved yet.
    'PENDING',      -- Fraud passed. Saga started. Account debit in progress.
    'POSTED',       -- TERMINAL. Debit + Credit succeeded. JournalEntries written.
    'FAILED',       -- TERMINAL. Saga compensation completed. Debit reversed.
    'REVERSED',     -- TERMINAL. Posted transaction reversed by manager/system.
    'BLOCKED',       -- TERMINAL. Fraud flagged. No money moved. No compensation needed.
    'DEBIT_SUCCESS',    -- Debit succeed
    'COMPENSATE_INITIATED', -- Compensation initiated. Credit failed
    'MANUAL_REVIEW',     -- Compensation exhausted
    'CREDIT_RETRY',    -- Credit failed.
    'TRANSFER'          -- Transfer initiated
    );

-- How the transaction was initiated.
-- Used for limit validation, fraud scoring, analytics, routing.
CREATE TYPE txn.transaction_channel AS ENUM (
    'BRANCH',            -- Physical branch teller
    'ATM',               -- ATM terminal (ISO 8583 → REST adapter)
    'INTERNET_BANKING',  -- Internet banking portal
    'MOBILE_APP',        -- Mobile banking app
    'UPI',               -- Unified Payments Interface
    'NEFT',              -- National Electronic Funds Transfer (batch)
    'RTGS',              -- Real Time Gross Settlement (min ₹2 lakh)
    'IMPS',              -- Immediate Payment Service (instant, 24x7)
    'POS',               -- Point of Sale terminal
    'API'                -- Direct API (corporate/fintech partners)
    );

-- Accounting direction for a JournalEntry.
-- DR = Debit  → reduces liability (customer balance goes DOWN for savings).
-- CR = Credit → increases liability (customer balance goes UP for savings).
-- NOT the same as TransactionType DEBIT/CREDIT — that is customer language.
CREATE TYPE txn.dr_cr AS ENUM (
    'DR',   -- Debit
    'CR'    -- Credit
    );

-- Whether a GL account maps to a customer product or is internal/suspense.
CREATE TYPE txn.gl_category AS ENUM (
    'CUSTOMER',     -- Savings, current, FD, RD — maps to customer-facing products
    'INTERNAL_GL'   -- Suspense, vault, fraud hold — never on customer statements
    );

-- Accounting classification of a GL account.
-- Determines which side (DR/CR) is the normal increasing direction.
-- ASSET/EXPENSE increase on DR. LIABILITY/INCOME increase on CR.
CREATE TYPE txn.gl_account_type AS ENUM (
    'ASSET',      -- What the bank owns: cash vault, nostro accounts
    'LIABILITY',  -- What the bank owes: customer deposits
    'INCOME',     -- Revenue earned: fees, interest on loans
    'EXPENSE'     -- Costs incurred: interest paid to customers
    );

-- INSTANT: settled_at set immediately on POSTED (UPI, IMPS, RTGS, internal).
-- DEFERRED: settled_at set by batch after RBI/CTS confirmation (NEFT, cheque).
CREATE TYPE txn.settlement_type AS ENUM (
    'INSTANT',
    'DEFERRED'
    );

-- How often a Standing Instruction executes.
CREATE TYPE txn.si_frequency AS ENUM (
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'QUARTERLY',
    'YEARLY'
    );

-- Lifecycle state of a Standing Instruction.
CREATE TYPE txn.si_status AS ENUM (
    'ACTIVE',      -- Eligible for batch job execution
    'PAUSED',      -- Temporarily disabled by customer
    'CANCELLED',   -- Permanently stopped. Create a new SI to restart.
    'COMPLETED',   -- max_executions reached. Auto-set by batch job.
    'FAILED'       -- Last execution failed (e.g. insufficient balance)
    );

-- Why an ATM transaction was declined. Null on successful dispensal.
CREATE TYPE txn.atm_reject_reason AS ENUM (
    'INSUFFICIENT_CASH',      -- ATM cassettes empty or insufficient
    'CARD_BLOCKED',           -- Card blocked by bank
    'DAILY_LIMIT_EXCEEDED',   -- Would exceed customer daily ATM limit
    'PIN_INCORRECT',          -- Wrong PIN entered
    'INSUFFICIENT_BALANCE',   -- Account has insufficient balance
    'TECHNICAL_FAILURE'       -- ATM hardware/network error
    );

-- CTS cheque clearing lifecycle.
CREATE TYPE txn.cheque_status AS ENUM (
    'PRESENTED',    -- Cheque received at branch, not yet in CTS
    'IN_CLEARING',  -- Cheque image submitted to CTS
    'CLEARED',      -- Drawee bank honoured. Transaction → POSTED.
    'BOUNCED',      -- Drawee bank rejected. Transaction → FAILED.
    'RETURNED'      -- Returned without presenting (post-dated, incomplete)
    );


-- =============================================================
-- TABLE 1: gl_accounts
-- =============================================================
CREATE TABLE txn.gl_accounts (
                                 gl_account_id   UUID                    NOT NULL DEFAULT gen_random_uuid(),
                                 gl_code         VARCHAR(30)             NOT NULL,
                                 name            VARCHAR(100)            NOT NULL,
                                 category        txn.gl_category         NOT NULL,
                                 account_type    txn.gl_account_type     NOT NULL,
    -- Which direction INCREASES this GL account.
    -- LIABILITY/INCOME → CR. ASSET/EXPENSE → DR.
                                 normal_balance  txn.dr_cr               NOT NULL,
                                 description     VARCHAR(255),
                                 is_active       BOOLEAN                 NOT NULL DEFAULT TRUE,

                                 CONSTRAINT pk_gl_account      PRIMARY KEY (gl_account_id),
                                 CONSTRAINT uq_gl_account_code UNIQUE      (gl_code)
);

COMMENT ON TABLE  txn.gl_accounts IS
    'General Ledger account categories for RBI regulatory reporting. '
        'Seeded at startup. Never modified at runtime. '
        'Leading digit 9 in gl_code = internal/suspense account.';

COMMENT ON COLUMN txn.gl_accounts.normal_balance IS
    'Which direction increases this GL account. '
        'LIABILITY/INCOME → CR increases. ASSET/EXPENSE → DR increases. '
        'Used by finance team to flag anomalies in regulatory reports.';


-- =============================================================
-- TABLE 2: transaction_type_configs
-- =============================================================
CREATE TABLE txn.transaction_type_configs (
    -- Matches txn.transaction_type ENUM value exactly.
                                              type_code            VARCHAR(30)          NOT NULL,
                                              name                 VARCHAR(100)         NOT NULL,
                                              gl_account_id        UUID                 NOT NULL,
                                              max_limit_daily      DECIMAL(20, 4)       NOT NULL,
                                              charge_applicable    BOOLEAN              NOT NULL DEFAULT FALSE,
                                              reversal_allowed     BOOLEAN              NOT NULL DEFAULT TRUE,
                                              settlement_type      txn.settlement_type  NOT NULL DEFAULT 'INSTANT',
                                              requires_beneficiary BOOLEAN              NOT NULL DEFAULT FALSE,
                                              is_active            BOOLEAN              NOT NULL DEFAULT TRUE,

                                              CONSTRAINT pk_txn_type_config     PRIMARY KEY (type_code),
                                              CONSTRAINT fk_txn_type_gl_account FOREIGN KEY (gl_account_id)
                                                  REFERENCES txn.gl_accounts (gl_account_id),
                                              CONSTRAINT chk_max_limit_positive CHECK (max_limit_daily > 0)
);

COMMENT ON TABLE txn.transaction_type_configs IS
    'Rule configuration per transaction type. '
        'Data-driven: change rules without redeployment. '
        'type_code matches txn.transaction_type enum values.';


-- =============================================================
-- TABLE 3: transactions
-- =============================================================
CREATE TABLE txn.transactions (
                                  txn_id                 UUID                      NOT NULL DEFAULT gen_random_uuid(),
                                  reference_number       VARCHAR(18)               NOT NULL,
                                  type                   txn.transaction_type      NOT NULL,
                                  type_code              VARCHAR(30)               NOT NULL,
    -- NULL only for inbound external transfers (NEFT/RTGS from another bank)
                                  source_account_id      UUID,
    -- NULL only for outbound external transfers
                                  destination_account_id UUID,
    -- "{IFSC}/{accountNumber}" for external party. Null for internal transfers.
                                  external_party_ref     VARCHAR(50),
                                  amount                 DECIMAL(20, 4)            NOT NULL,
                                  status                 txn.transaction_status    NOT NULL DEFAULT 'INITIATED',
                                  channel                txn.transaction_channel   NOT NULL,
                                  idempotency_key        VARCHAR(36),
                                  description            VARCHAR(200),
                                  initiated_by           UUID,
                                  created_at             TIMESTAMPTZ               NOT NULL DEFAULT NOW(),
    -- Null until POSTED. Proof that money moved.
                                  settled_at             TIMESTAMPTZ,
    -- Self-referential FK. Non-null only for REVERSAL type.
                                  reversal_txn_id        UUID,
                                  credit_retry_count     INTEGER                NOT NULL DEFAULT 0,
                                  compensate_retry_count INTEGER                NOT NULL DEFAULT 0,

                                  CONSTRAINT pk_transaction
                                      PRIMARY KEY (txn_id),

                                  CONSTRAINT uq_transaction_reference
                                      UNIQUE (reference_number),

                                  CONSTRAINT uq_transaction_idempotency_key
                                      UNIQUE (idempotency_key),

                                  CONSTRAINT fk_transaction_type_config
                                      FOREIGN KEY (type_code)
                                          REFERENCES txn.transaction_type_configs (type_code),

                                  CONSTRAINT fk_transaction_reversal
                                      FOREIGN KEY (reversal_txn_id)
                                          REFERENCES txn.transactions (txn_id),

    -- At least one side must be a Nivesh Bank account
                                  CONSTRAINT chk_txn_not_fully_external
                                      CHECK (source_account_id IS NOT NULL OR destination_account_id IS NOT NULL),

                                  CONSTRAINT chk_txn_amount_positive
                                      CHECK (amount > 0),

    -- REVERSAL type must always point to the original transaction
                                  CONSTRAINT chk_txn_reversal_ref
                                      CHECK (type != 'REVERSAL' OR reversal_txn_id IS NOT NULL),

    -- settled_at only valid on states where money has moved
                                  CONSTRAINT chk_txn_settled_at_terminal
                                      CHECK (settled_at IS NULL OR status IN ('POSTED', 'REVERSED'))
);

COMMENT ON TABLE txn.transactions IS
    'Master record for every transaction attempt. '
        'Immutable fields: amount, type, source/destination, created_at.';


-- =============================================================
-- TABLE 4: journal_entries
-- Immutable ledger. NEVER UPDATE OR DELETE.
-- Exactly 2 rows per transaction (1 DR + 1 CR).
-- Trigger below enforces the double-entry rule.
-- =============================================================
CREATE TABLE txn.journal_entries (
                                     entry_id        UUID            NOT NULL DEFAULT gen_random_uuid(),
                                     txn_id          UUID            NOT NULL,
                                     account_id      UUID            NOT NULL,
                                     gl_account_id   UUID            NOT NULL,
                                     dr_cr           txn.dr_cr       NOT NULL,
                                     amount          DECIMAL(20, 4)  NOT NULL,
    -- Balance of account AFTER this entry.
    -- Stored at write time — avoids SUM over all prior entries per statement query.
                                     running_balance DECIMAL(20, 4)  NOT NULL,
                                     narration       VARCHAR(200),
    -- Microsecond precision for intraday RBI GL sequencing.
                                     posted_at       TIMESTAMP(6)    NOT NULL DEFAULT NOW(),

                                     CONSTRAINT pk_journal_entry       PRIMARY KEY (entry_id),

                                     CONSTRAINT fk_je_transaction      FOREIGN KEY (txn_id)
                                         REFERENCES txn.transactions (txn_id),

                                     CONSTRAINT fk_je_gl_account       FOREIGN KEY (gl_account_id)
                                         REFERENCES txn.gl_accounts (gl_account_id),

                                     CONSTRAINT chk_je_amount_positive CHECK (amount > 0)
);

-- ── TRIGGER: Enforce double-entry bookkeeping ──────────────────
-- PostgreSQL CHECK constraints cannot use subqueries.
-- Trigger is the correct mechanism for cross-row constraints.
--
-- Rules enforced:
--   1. A transaction cannot have more than 2 journal entries.
--   2. When both entries exist: must be exactly 1 DR and 1 CR.
--   3. When both entries exist: DR amount must equal CR amount.
CREATE OR REPLACE FUNCTION txn.fn_enforce_double_entry()
    RETURNS TRIGGER AS $$
DECLARE
    v_entry_count INT;
    v_dr_count    INT;
    v_cr_count    INT;
    v_dr_amount   DECIMAL(20,4);
    v_cr_amount   DECIMAL(20,4);
BEGIN
    SELECT
        COUNT(*),
        SUM(CASE WHEN dr_cr = 'DR' THEN 1 ELSE 0 END),
        SUM(CASE WHEN dr_cr = 'CR' THEN 1 ELSE 0 END),
        SUM(CASE WHEN dr_cr = 'DR' THEN amount ELSE 0 END),
        SUM(CASE WHEN dr_cr = 'CR' THEN amount ELSE 0 END)
    INTO v_entry_count, v_dr_count, v_cr_count, v_dr_amount, v_cr_amount
    FROM txn.journal_entries
    WHERE txn_id = NEW.txn_id;

    IF v_entry_count > 2 THEN
        RAISE EXCEPTION
            'Double-entry violation: transaction % already has % entries. Max is 2.',
            NEW.txn_id, v_entry_count;
    END IF;

    IF v_entry_count = 2 THEN
        IF v_dr_count != 1 OR v_cr_count != 1 THEN
            RAISE EXCEPTION
                'Double-entry violation: transaction % must have exactly 1 DR and 1 CR.',
                NEW.txn_id;
        END IF;
        IF v_dr_amount != v_cr_amount THEN
            RAISE EXCEPTION
                'Double-entry violation: transaction % DR amount (%) != CR amount (%).',
                NEW.txn_id, v_dr_amount, v_cr_amount;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_double_entry
    AFTER INSERT ON txn.journal_entries
    FOR EACH ROW
EXECUTE FUNCTION txn.fn_enforce_double_entry();

COMMENT ON TABLE txn.journal_entries IS
    'Immutable ledger. NEVER UPDATE OR DELETE any row. '
        'Corrections are made by writing new REVERSAL entries. '
        'Trigger enforces: exactly 2 rows per txn (1 DR + 1 CR), amounts equal.';


-- =============================================================
-- TABLE 5: transaction_limits
-- =============================================================
CREATE TABLE txn.transaction_limits (
                                        limit_id      UUID                     NOT NULL DEFAULT gen_random_uuid(),
                                        account_id    UUID                     NOT NULL,
                                        channel       txn.transaction_channel  NOT NULL,
                                        daily_limit   DECIMAL(20, 4)           NOT NULL,
                                        per_txn_limit DECIMAL(20, 4)           NOT NULL,
                                        monthly_limit DECIMAL(20, 4)           NOT NULL,
    -- Running counters reset by midnight batch job.
                                        daily_used    DECIMAL(20, 4)           NOT NULL DEFAULT 0,
                                        monthly_used  DECIMAL(20, 4)           NOT NULL DEFAULT 0,
    -- Batch job checks: if last_reset < today → reset daily_used.
                                        last_reset    DATE                     NOT NULL DEFAULT CURRENT_DATE,
                                        updated_at    TIMESTAMPTZ              NOT NULL DEFAULT NOW(),

                                        CONSTRAINT pk_transaction_limit
                                            PRIMARY KEY (limit_id),

                                        CONSTRAINT uq_txn_limit_account_channel
                                            UNIQUE (account_id, channel),

                                        CONSTRAINT chk_daily_limit_positive   CHECK (daily_limit   > 0),
                                        CONSTRAINT chk_per_txn_limit_positive CHECK (per_txn_limit > 0),
                                        CONSTRAINT chk_monthly_limit_positive CHECK (monthly_limit > 0),
                                        CONSTRAINT chk_daily_used_lte_limit   CHECK (daily_used   <= daily_limit),
                                        CONSTRAINT chk_monthly_used_lte_limit CHECK (monthly_used <= monthly_limit),
                                        CONSTRAINT chk_per_txn_lte_daily      CHECK (per_txn_limit <= daily_limit)
);

COMMENT ON TABLE txn.transaction_limits IS
    'Per-account per-channel limits. '
        'daily_used and monthly_used stored as running counters — '
        'avoids expensive SUM queries on every transaction.';


-- =============================================================
-- TABLE 6: standing_instructions
-- =============================================================
CREATE TABLE txn.standing_instructions (
                                           si_id          UUID              NOT NULL DEFAULT gen_random_uuid(),
                                           account_id     UUID              NOT NULL,
                                           beneficiary_id UUID              NOT NULL,
                                           amount         DECIMAL(20, 4)   NOT NULL,
                                           frequency      txn.si_frequency  NOT NULL,
    -- MONTHLY: 1–28 (capped — never 29/30/31, February constraint).
    -- WEEKLY:  1=Sunday … 7=Saturday.
                                           execution_day  INTEGER,
                                           next_run_date  DATE              NOT NULL,
                                           start_date     DATE              NOT NULL,
                                           end_date       DATE,
                                           max_executions INT,
                                           executed_count INT               NOT NULL DEFAULT 0,
                                           status         txn.si_status     NOT NULL DEFAULT 'ACTIVE',
                                           failure_reason VARCHAR(100),
                                           created_by     UUID              NOT NULL,
                                           created_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                                           updated_at     TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

                                           CONSTRAINT pk_standing_instruction
                                               PRIMARY KEY (si_id),

    -- execution_day 1–28 for MONTHLY (never 29/30/31 — February constraint)
                                           CONSTRAINT chk_si_execution_day_monthly
                                               CHECK (frequency != 'MONTHLY' OR (execution_day >= 1 AND execution_day <= 28)),

    -- execution_day 1–7 for WEEKLY
                                           CONSTRAINT chk_si_execution_day_weekly
                                               CHECK (frequency != 'WEEKLY' OR (execution_day >= 1 AND execution_day <= 7)),

                                           CONSTRAINT chk_si_amount_positive
                                               CHECK (amount > 0),

                                           CONSTRAINT chk_si_executed_count_non_negative
                                               CHECK (executed_count >= 0),

                                           CONSTRAINT chk_si_date_range
                                               CHECK (end_date IS NULL OR end_date > start_date),

                                           CONSTRAINT chk_si_executed_lte_max
                                               CHECK (max_executions IS NULL OR executed_count <= max_executions)
);

COMMENT ON TABLE txn.standing_instructions IS
    'Pre-authorized recurring transactions executed by nightly SI batch job. '
        'execution_day capped at 28 — February has only 28 days in non-leap years.';


-- =============================================================
-- TABLE 7: atm_transactions
-- =============================================================
CREATE TABLE txn.atm_transactions (
                                      atm_txn_id          UUID                   NOT NULL DEFAULT gen_random_uuid(),
                                      txn_id              UUID                   NOT NULL,
                                      atm_id              UUID                   NOT NULL,
                                      card_id             UUID                   NOT NULL,
                                      pin_verified        BOOLEAN                NOT NULL,
                                      requested_amount    DECIMAL(20, 4)         NOT NULL,
    -- Actual cash dispensed. May be < requested if ATM ran low.
    -- Both stored for dispute resolution.
                                      dispensed_amount    DECIMAL(20, 4)         NOT NULL,
    -- Null on success.
                                      reject_reason       txn.atm_reject_reason,
    -- ISO 8583 sequence number for reconciliation with ATM vendor.
                                      atm_sequence_number VARCHAR(20),
                                      created_at          TIMESTAMPTZ            NOT NULL DEFAULT NOW(),

                                      CONSTRAINT pk_atm_transaction
                                          PRIMARY KEY (atm_txn_id),

                                      CONSTRAINT uq_atm_txn_txn_id
                                          UNIQUE (txn_id),

                                      CONSTRAINT fk_atm_txn_transaction
                                          FOREIGN KEY (txn_id) REFERENCES txn.transactions (txn_id),

                                      CONSTRAINT chk_atm_dispensed_non_negative
                                          CHECK (dispensed_amount >= 0),

                                      CONSTRAINT chk_atm_requested_positive
                                          CHECK (requested_amount > 0),

                                      CONSTRAINT chk_atm_dispensed_lte_requested
                                          CHECK (dispensed_amount <= requested_amount)
);

COMMENT ON TABLE txn.atm_transactions IS
    'Extension table for ATM-specific fields. One-to-one with transactions. '
        'dispensed_amount may differ from requested_amount if ATM ran low on cash.';


-- =============================================================
-- TABLE 8: cheque_transactions
-- =============================================================
CREATE TABLE txn.cheque_transactions (
                                         cheque_txn_id    UUID               NOT NULL DEFAULT gen_random_uuid(),
                                         txn_id           UUID               NOT NULL,
    -- 6-digit leaf number from cheque book.
                                         cheque_number    VARCHAR(6)            NOT NULL,
    -- IFSC of the bank on which the cheque is drawn (payer's bank).
                                         drawee_bank_ifsc VARCHAR(11)           NOT NULL,
    -- 9-digit MICR code: city(3) + bank(3) + branch(3).
    -- Printed in magnetic ink. Read by CTS scanner — never manually entered.
                                         micr_code        VARCHAR(9)            NOT NULL,
                                         presented_date   DATE               NOT NULL,
    -- T+1 local, T+2 outstation.
                                         clearing_date    DATE               NOT NULL,
                                         status           txn.cheque_status  NOT NULL DEFAULT 'PRESENTED',
    -- Null until BOUNCED.
                                         bounce_reason    VARCHAR(50),
    -- RBI CTS reference for inter-bank reconciliation.
                                         cts_reference    VARCHAR(30),
                                         created_at       TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
                                         updated_at       TIMESTAMPTZ        NOT NULL DEFAULT NOW(),

                                         CONSTRAINT pk_cheque_transaction
                                             PRIMARY KEY (cheque_txn_id),

                                         CONSTRAINT uq_cheque_txn_txn_id
                                             UNIQUE (txn_id),

                                         CONSTRAINT fk_cheque_txn_transaction
                                             FOREIGN KEY (txn_id) REFERENCES txn.transactions (txn_id),

    -- bounce_reason only allowed when status is BOUNCED
                                         CONSTRAINT chk_cheque_bounce_reason
                                             CHECK (bounce_reason IS NULL OR status = 'BOUNCED'),

                                         CONSTRAINT chk_cheque_clearing_after_presented
                                             CHECK (clearing_date >= presented_date),

                                         CONSTRAINT chk_micr_code_numeric
                                             CHECK (micr_code ~ '^[0-9]{9}$'),

                                         CONSTRAINT chk_cheque_number_numeric
                                             CHECK (cheque_number ~ '^[0-9]{6}$'),

                                         CONSTRAINT chk_drawee_ifsc_length
                                             CHECK (LENGTH(drawee_bank_ifsc) = 11)
);

COMMENT ON TABLE txn.cheque_transactions IS
    'Extension table for CTS cheque clearing. One-to-one with transactions. '
        'Transaction stays PENDING until CTS confirms CLEARED.';


-- =============================================================
-- SUMMARY
-- Enum types created:
--   txn.transaction_type     txn.transaction_status  txn.transaction_channel
--   txn.dr_cr                txn.gl_category         txn.gl_account_type
--   txn.settlement_type      txn.si_frequency        txn.si_status
--   txn.atm_reject_reason    txn.cheque_status
--
-- Tables created (in FK dependency order):
--   txn.gl_accounts            txn.transaction_type_configs
--   txn.transactions           txn.journal_entries  (+ double-entry trigger)
--   txn.transaction_limits     txn.standing_instructions
--   txn.atm_transactions       txn.cheque_transactions
--
-- Next migration: V2__seed_gl_accounts.sql
-- =============================================================
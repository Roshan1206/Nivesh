-- =============================================================
-- Nivesh Bank — Transaction Service
-- Migration: V2__seed_gl_and_type_configs.sql
-- Seeds: txn.gl_accounts, txn.transaction_type_configs
-- =============================================================

-- =============================================================
-- SECTION 1: gl_accounts
-- Covers:
--   CUSTOMER  → Savings, Current, FD, RD (LIABILITY)
--   INTERNAL  → Vault cash, Nostro, Suspense, Fee Income,
--               Interest Expense, Fraud Hold, Reversal Clearing
-- GL code convention:
--   1xxxxx  → ASSET
--   2xxxxx  → LIABILITY (customer-facing)
--   4xxxxx  → INCOME
--   5xxxxx  → EXPENSE
--   9xxxxx  → INTERNAL / SUSPENSE
-- =============================================================

INSERT INTO txn.gl_accounts
(gl_account_id, gl_code, name, category, account_type, normal_balance, description, is_active)
VALUES

-- ── ASSET accounts ────────────────────────────────────────────
(gen_random_uuid(), '100100', 'Cash Vault — Head Office',
 'INTERNAL_GL', 'ASSET', 'DR',
 'Physical cash held in HO vault. Increases on ATM replenishment; decreases on ATM dispense.', TRUE),

(gen_random_uuid(), '100200', 'Cash Vault — Branch Network',
 'INTERNAL_GL', 'ASSET', 'DR',
 'Aggregate cash across all branch teller drawers. Reconciled nightly.', TRUE),

(gen_random_uuid(), '100300', 'Nostro Account — RBI Settlement',
 'INTERNAL_GL', 'ASSET', 'DR',
 'Nivesh Bank account maintained at RBI for NEFT/RTGS settlement. Funded daily.', TRUE),

(gen_random_uuid(), '100400', 'Nostro Account — IMPS / UPI Float',
 'INTERNAL_GL', 'ASSET', 'DR',
 'NPCI-side float account. Holds funds in transit for UPI and IMPS real-time settlements.', TRUE),

(gen_random_uuid(), '100500', 'ATM Cash Float',
 'INTERNAL_GL', 'ASSET', 'DR',
 'Cash loaded into ATM cassettes across the network. Decremented on successful dispense.', TRUE),

-- ── LIABILITY accounts (customer-facing) ──────────────────────
(gen_random_uuid(), '200100', 'Customer Savings Accounts',
 'CUSTOMER', 'LIABILITY', 'CR',
 'Aggregate liability for all active savings account balances. Primary DR/CR GL for DEBIT and CREDIT transactions.', TRUE),

(gen_random_uuid(), '200200', 'Customer Current Accounts',
 'CUSTOMER', 'LIABILITY', 'CR',
 'Aggregate liability for current (checking) account balances. Used for high-volume business transactions.', TRUE),

(gen_random_uuid(), '200300', 'Fixed Deposit Accounts',
 'CUSTOMER', 'LIABILITY', 'CR',
 'Aggregate FD corpus held by the bank. Decreased at maturity payout.', TRUE),

(gen_random_uuid(), '200400', 'Recurring Deposit Accounts',
 'CUSTOMER', 'LIABILITY', 'CR',
 'Aggregate RD corpus. Instalments credited here monthly via Standing Instruction.', TRUE),

-- ── INCOME accounts ───────────────────────────────────────────
(gen_random_uuid(), '400100', 'Fee Income — Service Charges',
 'INTERNAL_GL', 'INCOME', 'CR',
 'Revenue from transaction fees: NEFT charges, IMPS charges, cheque processing, etc.', TRUE),

(gen_random_uuid(), '400200', 'Fee Income — ATM Interchange',
 'INTERNAL_GL', 'INCOME', 'CR',
 'Interchange income earned when non-Nivesh cardholders use Nivesh ATMs.', TRUE),

(gen_random_uuid(), '400300', 'Interest Income — Loans & Advances',
 'INTERNAL_GL', 'INCOME', 'CR',
 'Interest earned on retail and corporate loan portfolio. Not directly used in deposit transactions.', TRUE),

-- ── EXPENSE accounts ──────────────────────────────────────────
(gen_random_uuid(), '500100', 'Interest Expense — Savings Accounts',
 'INTERNAL_GL', 'EXPENSE', 'DR',
 'Interest paid to savings account holders. Credited to customer accounts via INTEREST transaction type.', TRUE),

(gen_random_uuid(), '500200', 'Interest Expense — Fixed Deposits',
 'INTERNAL_GL', 'EXPENSE', 'DR',
 'Interest accrued and paid at FD maturity. Booked here; paid out via FD_MATURITY type.', TRUE),

(gen_random_uuid(), '500300', 'Interest Expense — Recurring Deposits',
 'INTERNAL_GL', 'EXPENSE', 'DR',
 'Interest component on RD payouts.', TRUE),

-- ── INTERNAL / SUSPENSE accounts ──────────────────────────────
(gen_random_uuid(), '900100', 'Suspense — Unreconciled Credits',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Temporary parking for inbound NEFT/RTGS where destination account cannot be identified. Must be cleared within T+1.', TRUE),

(gen_random_uuid(), '900200', 'Suspense — Unreconciled Debits',
 'INTERNAL_GL', 'ASSET', 'DR',
 'Temporary parking for outbound payments pending confirmation from correspondent bank.', TRUE),

(gen_random_uuid(), '900300', 'Fraud Hold — Blocked Funds',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Funds frozen by Fraud Service. Released to customer on clearance or forfeited on confirmed fraud.', TRUE),

(gen_random_uuid(), '900400', 'Reversal Clearing Account',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Transit GL used during REVERSAL saga. DR on reversal initiation, CR when original customer account is restored.', TRUE),

(gen_random_uuid(), '900500', 'Refund Payable — Regulatory',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Holds refund amounts approved by compliance/RBI before disbursement to customer.', TRUE),

(gen_random_uuid(), '900600', 'Standing Instruction Transit',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Intraday transit GL for SI batch. Debited when SI executes, credited when funds reach beneficiary.', TRUE),

(gen_random_uuid(), '900700', 'POS Interchange Payable',
 'INTERNAL_GL', 'LIABILITY', 'CR',
 'Amounts owed to acquiring banks for POS transactions. Settled via NPCI batch T+1.', TRUE);


-- =============================================================
-- SECTION 2: transaction_type_configs
-- One row per txn.transaction_type enum value (8 values).
-- gl_account_id resolved by subquery on gl_code — no hardcoded UUIDs.
-- max_limit_daily: RBI-mandated where rules exist, permissive otherwise.
--
-- RBI reference limits applied:
--   RTGS      → min ₹2,00,000 (no official upper cap; set to ₹10 Cr)
--   NEFT      → no RBI cap; set to ₹10,00,000 (bank policy)
--   UPI       → RBI cap ₹1,00,000 (P2P); ₹5,00,000 for specific categories
--   ATM       → RBI daily cap ₹20,000 for basic savings; ₹1,00,000 for full KYC
--   IMPS      → RBI cap ₹5,00,000 per transaction
-- =============================================================

INSERT INTO txn.transaction_type_configs
(type_code, name, gl_account_id, max_limit_daily,
 charge_applicable, reversal_allowed, settlement_type,
 requires_beneficiary, is_active)
VALUES

-- DEBIT — Customer-initiated outbound payment
-- GL: Customer Savings Accounts (primary settlement GL for outbound money movement)
-- Limit: permissive bank policy ₹10,00,000; per-channel caps enforced in transaction_limits
(
    'DEBIT',
    'Customer Debit — Outbound Payment',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '200100'),
    1000000.0000,   -- ₹10,00,000 daily; channel-level limits in transaction_limits table
    FALSE,          -- base debit has no fee; channel-specific fees applied separately
    TRUE,
    'INSTANT',
    TRUE,           -- must identify where money is going
    TRUE
),

-- CREDIT — Inbound money to customer account
-- GL: Customer Savings Accounts (liability increases on CR)
-- Limit: no regulatory cap on inbound; set high
(
    'CREDIT',
    'Customer Credit — Inbound Payment',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '200100'),
    50000000.0000,  -- ₹5 Cr — effectively uncapped for inbound; fraud scoring is the gate
    FALSE,
    FALSE,          -- you don't reverse a credit; you raise a REVERSAL transaction
    'INSTANT',
    FALSE,          -- inbound: beneficiary is always us
    TRUE
),

-- REVERSAL — Technical undo of a POSTED transaction
-- GL: Reversal Clearing Account (transit GL; saga moves money back to customer)
-- Limit: match original transaction; set permissive — actual cap comes from original txn
(
    'REVERSAL',
    'Transaction Reversal',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '900400'),
    50000000.0000,  -- permissive; constrained by the original txn amount
    FALSE,
    FALSE,          -- a reversal itself cannot be reversed; raise a new corrective txn
    'INSTANT',
    FALSE,
    TRUE
),

-- FEE — Service charge levied by the bank
-- GL: Fee Income — Service Charges (income increases on CR)
-- Limit: fees are small; ₹10,000 daily cap is safe headroom
(
    'FEE',
    'Bank Service Fee',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '400100'),
    10000.0000,     -- ₹10,000; fees are system-generated, rarely large
    TRUE,           -- this IS the charge; charge_applicable = TRUE by definition
    TRUE,           -- erroneous fees must be reversible
    'INSTANT',
    FALSE,          -- fee debited from customer account; no external beneficiary
    TRUE
),

-- INTEREST — Interest credited to savings/FD/RD
-- GL: Interest Expense — Savings Accounts (expense DR offsets customer CR)
-- Limit: permissive; interest is calculated by core banking, not user-initiated
(
    'INTEREST',
    'Interest Credit',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '500100'),
    10000000.0000,  -- ₹1 Cr; large FD interest payouts must be accommodated
    FALSE,
    TRUE,           -- erroneous interest credits must be reversible by ops
    'INSTANT',
    FALSE,
    TRUE
),

-- REFUND — Goodwill / regulatory return to customer
-- GL: Refund Payable — Regulatory (held in suspense until disbursed)
-- Limit: permissive; regulated refunds can be large
(
    'REFUND',
    'Customer Refund',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '900500'),
    5000000.0000,   -- ₹50,00,000; large regulatory refunds must pass through
    FALSE,
    TRUE,
    'INSTANT',
    FALSE,          -- refund goes back to originating customer account
    TRUE
),

-- STANDING_INSTRUCTION — Auto-debit from SI batch job
-- GL: Standing Instruction Transit (intraday batch GL)
-- Limit: ₹10,00,000 per RBI SI guidelines for auto-debit mandates
(
    'STANDING_INSTRUCTION',
    'Standing Instruction Auto-Debit',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '900600'),
    1000000.0000,   -- ₹10,00,000 — RBI NACH mandate limit for auto-debit
    FALSE,
    TRUE,
    'DEFERRED',     -- SI batch settles via NEFT/NACH; not real-time
    TRUE,           -- always has a pre-registered beneficiary
    TRUE
),

-- FD_MATURITY — Fixed Deposit maturity payout to customer
-- GL: Interest Expense — Fixed Deposits (principal + interest both flow through here)
-- Limit: permissive; FD corpus can be large (₹5 Cr single FD allowed at Nivesh)
(
    'FD_MATURITY',
    'Fixed Deposit Maturity Payout',
    (SELECT gl_account_id FROM txn.gl_accounts WHERE gl_code = '500200'),
    50000000.0000,  -- ₹5 Cr; large FDs must mature without hitting a daily cap
    FALSE,
    FALSE,          -- maturity is a terminal event; no reversal (use REFUND if error)
    'INSTANT',
    FALSE,          -- pays out to the FD-linked savings account; no external beneficiary
    TRUE
);


-- =============================================================
-- VERIFICATION QUERIES (run manually to confirm seed)
-- =============================================================
-- SELECT gl_code, name, category, account_type, normal_balance
-- FROM txn.gl_accounts ORDER BY gl_code;
--
-- SELECT tc.type_code, tc.name, ga.gl_code, ga.name AS gl_name,
--        tc.max_limit_daily, tc.charge_applicable, tc.settlement_type
-- FROM txn.transaction_type_configs tc
-- JOIN txn.gl_accounts ga ON ga.gl_account_id = tc.gl_account_id
-- ORDER BY tc.type_code;
-- =============================================================
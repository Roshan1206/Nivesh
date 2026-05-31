CREATE TYPE payout_type_enum AS ENUM (
'MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY', 'MATURITY'
);

CREATE TYPE status_enum AS ENUM (
'ACTIVE', 'FROZE', 'DORMANT', 'CLOSED', 'PENDING', 'ACTIVATION', 'MATURED', 'PREMATURELY_CLOSED', 'RENEWED'
);

CREATE SEQUENCE seq_saving_acc_prefix_1 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_current_acc_prefix_2 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_fd_prefix_3 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_rd_prefix_4 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_nre_account_prefix_5 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_nro_account_prefix_6 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_fcnr_account_prefix_7 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_salary_account_prefix_8 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_jan_dhan_account_prefix_9 START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;

CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_code CHAR(3) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL UNIQUE,
    min_balance DECIMAL(20,2) NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    max_withdrawal_limit DECIMAL(20,2) NOT NULL,
    features JSONB,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE
    customer_number VARCHAR(8) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    balance DECIMAL(20,2) NOT NULL,
    available_balance DECIMAL(20,2) NOT NULL,
    status status_enum NOT NULL,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    interest_rate DECIMAL(4,2),
    nomination_id UUID,
    product_id UUID
);

CREATE TABLE fixed_deposits(
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE,
    principal DECIMAL(20,2) NOT NULL,
    tenure INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    maturity_date DATE NOT NULL,
    last_interest_payout DATE,
    next_interest_payout DATE NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    maturity_amount DECIMAL(20,2) NOT NULL,
    payout_type payout_type_enum NOT NULL,
    auto_renewable BOOLEAN NOT NULL,
    status status_enum NOT NULL,
    closed_at DATE,
    nomination_id UUID,
    account_id UUID
);

CREATE TABLE recurring_deposits(
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE,
    installment DECIMAL(20,2) NOT NULL,
    tenure INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    maturity_date DATE NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    maturity_amount DECIMAL(20,2) NOT NULL,
    status status_enum NOT NULL,
    next_installment_date DATE NOT NULL,
    missed_count INTEGER,
    closed_at DATE,
    nomination_id UUID,
    account_id UUID
);

ALTER TABLE accounts ADD CONSTRAINT fk_account_product FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE fixed_deposits ADD CONSTRAINT fk_fd_account FOREIGN KEY (account_id) REFERENCES accounts(id);
ALTER TABLE recurring_deposits ADD CONSTRAINT fk_rd_account FOREIGN KEY (account_id) REFERENCES accounts(id);

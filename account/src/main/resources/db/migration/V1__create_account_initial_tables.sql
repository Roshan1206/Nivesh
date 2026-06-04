CREATE TYPE payout_type_enum AS ENUM ('MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY', 'MATURITY');
CREATE TYPE status_enum AS ENUM ('ACTIVE', 'FROZE', 'DORMANT', 'CLOSED', 'PENDING', 'ACTIVATION', 'MATURED', 'PREMATURELY_CLOSED', 'RENEWED');

CREATE SEQUENCE seq_saving START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_current START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_fd START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_rd START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_premium START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;
CREATE SEQUENCE seq_salary START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999 NO CYCLE CACHE 100;

CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_code VARCHAR(3) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL UNIQUE,
    product_prefix VARCHAR(2) NOT NULL UNIQUE,
    sequence_name VARCHAR(100) NOT NULL UNIQUE,
    min_balance DECIMAL(20,2) NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    max_withdrawal_limit DECIMAL(20,2) NOT NULL,
    features JSONB,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100)
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE,
    customer_number VARCHAR(8) NOT NULL,
    balance DECIMAL(20,2) NOT NULL,
    available_balance DECIMAL(20,2) NOT NULL,
    hold_balance DECIMAL(20,2),
    status status_enum NOT NULL,
    interest_rate DECIMAL(4,2),
    nomination_id UUID,
    product_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

CREATE TABLE fixed_deposits(
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE,
    principal DECIMAL(20,2) NOT NULL,
    tenure INTEGER NOT NULL,
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
    account_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100)
);

CREATE TABLE recurring_deposits(
    id UUID PRIMARY KEY,
    account_number VARCHAR(11) NOT NULL UNIQUE,
    installment DECIMAL(20,2) NOT NULL,
    tenure INTEGER NOT NULL,
    maturity_date DATE NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    maturity_amount DECIMAL(20,2) NOT NULL,
    status status_enum NOT NULL,
    next_installment_date DATE NOT NULL,
    missed_count INTEGER,
    closed_at DATE,
    nomination_id UUID,
    account_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR(100)
);

ALTER TABLE accounts ADD CONSTRAINT fk_account_product FOREIGN KEY (product_id) REFERENCES products(id);
ALTER TABLE fixed_deposits ADD CONSTRAINT fk_fd_account FOREIGN KEY (account_id) REFERENCES accounts(id);
ALTER TABLE recurring_deposits ADD CONSTRAINT fk_rd_account FOREIGN KEY (account_id) REFERENCES accounts(id);

CREATE TYPE operation_type_enum AS ENUM ('DEBIT', 'CREDIT');
CREATE TABLE idempotency_records (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    idempotency_key      VARCHAR(255)   NOT NULL,
    type            VARCHAR(10)    NOT NULL,
    account_id           UUID           NOT NULL,
    amount               DECIMAL(20, 4) NOT NULL,
    running_balance      DECIMAL(20, 4) NOT NULL,
    response_status_code INT            NOT NULL,
    expires_at           TIMESTAMP      NOT NULL,
    created_at           TIMESTAMP      NOT NULL DEFAULT now(),
    created_by           VARCHAR(255),
    updated_at          TIMESTAMP      NOT NULL DEFAULT now(),
    updated_by          VARCHAR(255),
    CONSTRAINT pk_idempotency_records PRIMARY KEY (id),
    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key)
)

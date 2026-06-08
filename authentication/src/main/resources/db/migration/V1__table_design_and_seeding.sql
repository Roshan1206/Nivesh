-- ============================================================
-- Nivesh Bank — Auth Service
-- V1: Full Schema + Seed (tables, enums, roles, permissions)
-- ============================================================
-- DESIGN DECISIONS RECORDED HERE:
--
--  permission_code  VARCHAR(100) — VARCHAR(50) too tight for future strings
--  role_name        VARCHAR(30)  — CUSTOMER_KYC_PENDING = 20 chars, widen for safety
--  password         no UNIQUE    — BCrypt hashes of same password differ; UNIQUE is wrong
--  token_refresh_signals         — omitted; new token is issued on the spot on role change
--  CUSTOMER role                 — base marker only (PROFILE:READ/WRITE:OWN); no account
--                                  permissions — those belong to CUSTOMER_ACTIVE stage role
--  user_role_history             — append-only audit trail, never deleted
--  user_permission_overrides     — per-user GRANT/REVOKE exceptions on top of role defaults
-- ============================================================

SET search_path TO auth;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. ENUMS
-- ============================================================

CREATE TYPE customer_status_enum AS ENUM (
    'ONBOARDED',      -- registered, KYC not yet submitted
    'ACTIVE',         -- KYC approved, full banking access
    'REGISTERED',
    'LOCKED',         -- temporary lock after failed attempts
    'DEACTIVATED'     -- permanently closed
);

CREATE TYPE action_enum AS ENUM (
    'READ',
    'WRITE',
    'APPROVE',
    'ADMIN'
);

CREATE TYPE role_change_action_enum AS ENUM (
    'ASSIGNED',
    'REMOVED'
);

-- Reason is the trigger that caused the role change.
-- Used in user_role_history for compliance audit.
CREATE TYPE role_change_reason_enum AS ENUM (
    'REGISTRATION_COMPLETE',   -- user registered → CUSTOMER_REGISTERED assigned
    'KYC_SUBMITTED',           -- customer submitted KYC docs → CUSTOMER_KYC_PENDING assigned
    'KYC_APPROVED',            -- KYC approved by admin → CUSTOMER_ACTIVE assigned
    'KYC_REJECTED',            -- KYC rejected → revert to CUSTOMER_REGISTERED
    'MANUAL_ADMIN_GRANT',      -- admin manually granted a role
    'MANUAL_ADMIN_REVOKE',     -- admin manually revoked a role
    'ACCOUNT_LOCKED',          -- security lock applied
    'ACCOUNT_DEACTIVATED'      -- account permanently closed
);

CREATE TYPE override_type_enum AS ENUM (
    'GRANT',   -- give user a permission their role does not have
    'REVOKE'   -- block a permission their role would normally grant
);


-- ============================================================
-- 2. SPRING AUTHORIZATION SERVER TABLES (unchanged)
-- ============================================================

CREATE TABLE oauth2_registered_client (
    id                            VARCHAR(100)  NOT NULL,
    client_id                     VARCHAR(100)  NOT NULL,
    client_id_issued_at           TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_secret                 VARCHAR(200)  DEFAULT NULL,
    client_secret_expires_at      TIMESTAMPTZ   DEFAULT NULL,
    client_name                   VARCHAR(200)  NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types     VARCHAR(1000) NOT NULL,
    redirect_uris                 VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris     VARCHAR(1000) DEFAULT NULL,
    scopes                        VARCHAR(1000) NOT NULL,
    client_settings               VARCHAR(2000) NOT NULL,
    token_settings                VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization (
    id                            VARCHAR(100) NOT NULL,
    registered_client_id          VARCHAR(100) NOT NULL,
    principal_name                VARCHAR(200) NOT NULL,
    authorization_grant_type      VARCHAR(100) NOT NULL,
    authorized_scopes             VARCHAR(1000) DEFAULT NULL,
    attributes                    TEXT          DEFAULT NULL,
    state                         VARCHAR(500)  DEFAULT NULL,
    authorization_code_value      TEXT          DEFAULT NULL,
    authorization_code_issued_at  TIMESTAMPTZ   DEFAULT NULL,
    authorization_code_expires_at TIMESTAMPTZ   DEFAULT NULL,
    authorization_code_metadata   TEXT          DEFAULT NULL,
    access_token_value            TEXT          DEFAULT NULL,
    access_token_issued_at        TIMESTAMPTZ   DEFAULT NULL,
    access_token_expires_at       TIMESTAMPTZ   DEFAULT NULL,
    access_token_metadata         TEXT          DEFAULT NULL,
    access_token_type             VARCHAR(100)  DEFAULT NULL,
    access_token_scopes           VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value           TEXT          DEFAULT NULL,
    oidc_id_token_issued_at       TIMESTAMPTZ   DEFAULT NULL,
    oidc_id_token_expires_at      TIMESTAMPTZ   DEFAULT NULL,
    oidc_id_token_metadata        TEXT          DEFAULT NULL,
    refresh_token_value           TEXT          DEFAULT NULL,
    refresh_token_issued_at       TIMESTAMPTZ   DEFAULT NULL,
    refresh_token_expires_at      TIMESTAMPTZ   DEFAULT NULL,
    refresh_token_metadata        TEXT          DEFAULT NULL,
    user_code_value               TEXT          DEFAULT NULL,
    user_code_issued_at           TIMESTAMPTZ   DEFAULT NULL,
    user_code_expires_at          TIMESTAMPTZ   DEFAULT NULL,
    user_code_metadata            TEXT          DEFAULT NULL,
    device_code_value             TEXT          DEFAULT NULL,
    device_code_issued_at         TIMESTAMPTZ   DEFAULT NULL,
    device_code_expires_at        TIMESTAMPTZ   DEFAULT NULL,
    device_code_metadata          TEXT          DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name       VARCHAR(200) NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);


-- ============================================================
-- 3. CORE TABLES
-- ============================================================

CREATE TABLE users (
    user_id         UUID                 PRIMARY KEY,
    mobile_number   VARCHAR(10)          NOT NULL UNIQUE,
    email           VARCHAR(50)          NOT NULL UNIQUE,
    password        VARCHAR(100)         NOT NULL,          -- BCrypt hash; no UNIQUE (wrong for hashes)
    failed_attempt  INTEGER              NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ          DEFAULT NULL,
    customer_status customer_status_enum NOT NULL,
    token_version   INTEGER              NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ          NOT NULL,
    created_by      VARCHAR(50)          NOT NULL,
    updated_at      TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(50)          NOT NULL DEFAULT 'SYSTEM'
);

CREATE TABLE roles (
    role_id         UUID        PRIMARY KEY,
    role_name       VARCHAR(30) NOT NULL UNIQUE,   -- widened from 20; CUSTOMER_KYC_PENDING = 20 chars
    description     VARCHAR(200) NOT NULL,
    is_system_role  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(50) NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(50) NOT NULL DEFAULT 'SYSTEM'
);

CREATE TABLE permissions (
    permission_id   UUID         PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,  -- widened from 50; format: SVC:RESOURCE:ACTION[:SCOPE]
    resource        VARCHAR(50)  NOT NULL,
    action_granted  action_enum  NOT NULL,
    description     VARCHAR(200) NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY ,
    token_id UUID NOT NULL UNIQUE,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_AT TIMESTAMPTZ NOT NULL,
    revoked boolean NOT NULL DEFAULT false,
    revoked_at TIMESTAMPTZ,
    revoked_reason VARCHAR(200),
    user_id UUID NOT NULL,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Role → Permission (default grants; shared across all users with that role)
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(role_id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

-- User → Role (additive; a customer can hold multiple stage roles simultaneously)
CREATE TABLE user_roles (
    user_id     UUID        NOT NULL,
    role_id     UUID        NOT NULL,
    assigned_by UUID        NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user        FOREIGN KEY (user_id)     REFERENCES users(user_id),
    CONSTRAINT fk_ur_role        FOREIGN KEY (role_id)     REFERENCES roles(role_id),
    CONSTRAINT fk_ur_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

-- Append-only audit trail of every role ASSIGNED or REMOVED.
-- Never delete rows. Used for compliance and security audits.
CREATE TABLE user_role_history (
    id          UUID                    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID                    NOT NULL,
    role_id     UUID                    NOT NULL,
    action      role_change_action_enum NOT NULL,
    reason      role_change_reason_enum NOT NULL,
    changed_by  UUID                    NOT NULL,
    changed_at  TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_urh_user       FOREIGN KEY (user_id)    REFERENCES users(user_id),
    CONSTRAINT fk_urh_role       FOREIGN KEY (role_id)    REFERENCES roles(role_id),
    CONSTRAINT fk_urh_changed_by FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

CREATE INDEX idx_urh_user_id    ON user_role_history (user_id);
CREATE INDEX idx_urh_changed_at ON user_role_history (changed_at);

-- Per-user permission exceptions on top of role defaults.
-- GRANT  = extra permission beyond what their role provides.
-- REVOKE = blocks a permission their role would normally grant.
-- expires_at NULL = never expires.
CREATE TABLE user_permission_overrides (
    user_id       UUID               NOT NULL,
    permission_id UUID               NOT NULL,
    override_type override_type_enum NOT NULL,
    reason        VARCHAR(200)       NOT NULL,
    granted_by    UUID               NOT NULL,
    granted_at    TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMPTZ        DEFAULT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_upo_user        FOREIGN KEY (user_id)       REFERENCES users(user_id),
    CONSTRAINT fk_upo_permission  FOREIGN KEY (permission_id) REFERENCES permissions(permission_id),
    CONSTRAINT fk_upo_granted_by  FOREIGN KEY (granted_by)    REFERENCES users(user_id)
);

CREATE INDEX idx_upo_user_id ON user_permission_overrides (user_id);


-- ============================================================
-- 4. SEED — SYSTEM USER
-- ============================================================
INSERT INTO users (
    user_id, mobile_number, email, password,
    failed_attempt, locked_until, customer_status,
    created_at, created_by, updated_at, updated_by
) VALUES (
    gen_random_uuid(),
    '0000000000',
    'system@nvsh.com',
    '$2a$12$bVnFPMbHqjIv.WNIajOJxOzFqgP/YD9ANy9pJnm1e/y1v6B1tV2ge', -- BCrypt of Password@1234
    0, NULL, 'ACTIVE',
    NOW(), 'system', NOW(), 'system'
);


-- ============================================================
-- 5. SEED — ROLES
-- ============================================================
-- Staff roles (is_system_role = TRUE)
-- Customer stage roles (is_system_role = FALSE)
-- CUSTOMER = base marker only; no permissions of its own.
-- ============================================================
INSERT INTO roles (role_id, role_name, description, is_system_role, created_at, created_by, updated_at, updated_by) VALUES
    -- Staff
    (gen_random_uuid(), 'SUPER_ADMIN',          'Super administrator with all permissions',                        TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'ADMIN',                'Bank administrator for customer and account management',          TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'BRANCH_MGR',           'Branch manager with operational authority',                       TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'RM',                   'Relationship manager handling assigned customers',                TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'TELLER',               'Bank teller for front-desk customer operations',                  TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'AUDITOR',              'Internal auditor with read-only access across services',          TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'FRAUD_ANALYST',        'Fraud analyst for account monitoring and freezing',               TRUE,  NOW(), 'system', NOW(), 'system'),
    -- Customer base marker (no permissions; holds stage roles below)
    (gen_random_uuid(), 'CUSTOMER',             'Base marker role — identifies a user as a bank customer',         FALSE, NOW(), 'system', NOW(), 'system'),
    -- Customer stage roles (permissions grow with each stage)
    (gen_random_uuid(), 'CUSTOMER_REGISTERED',  'Registered customer — profile access only, KYC not yet started', FALSE, NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'CUSTOMER_ACTIVE',      'Fully KYC-verified customer with complete banking access',        FALSE, NOW(), 'system', NOW(), 'system');


-- ============================================================
-- 6. SEED — PERMISSIONS
-- ============================================================

-- ── Customer Service ────────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:OWN',      'customer-service', 'READ',    'View own customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:WRITE:OWN',     'customer-service', 'WRITE',   'Update own customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:ANY',      'customer-service', 'READ',    'View any customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:ASSIGNED', 'customer-service', 'READ',    'View profiles of assigned customers (RM only)'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:WRITE:ANY',     'customer-service', 'WRITE',   'Update any customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:KYC:READ:OWN',          'customer-service', 'READ',    'View own KYC submission and status'),
    (gen_random_uuid(), 'CUSTOMER_SVC:KYC:WRITE:OWN',         'customer-service', 'WRITE',   'Submit or update own KYC documents'),
    (gen_random_uuid(), 'CUSTOMER_SVC:KYC:APPROVE',           'customer-service', 'APPROVE', 'Verify and approve customer KYC');

-- ── Account Service ─────────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:OWN',                    'account-service', 'READ',    'View own bank accounts'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN',                  'account-service', 'WRITE',   'Open a new bank account for self'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN_ON_BEHALF',        'account-service', 'WRITE',   'Open a bank account on behalf of an assigned customer (RM only)'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:APPROVE:CLOSE',               'account-service', 'APPROVE', 'Close a customer bank account'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:APPROVE:FREEZE',              'account-service', 'APPROVE', 'Freeze a customer bank account'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:ANY',                    'account-service', 'READ',    'View all bank accounts across customers'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:ASSIGNED',               'account-service', 'READ',    'View accounts of assigned customers (RM only)'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:INTERNAL_DEBIT_CREDIT', 'account-service', 'WRITE',   'Internal debit/credit — service-to-service only, never exposed to users');

-- ── Transaction Service ─────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'TRANSACTION_SVC:TRANSACTION:READ:OWN',   'transaction-service', 'READ',  'View own transaction history'),
    (gen_random_uuid(), 'TRANSACTION_SVC:TRANSACTION:CREATE:OWN', 'transaction-service', 'WRITE', 'Initiate a transaction from own account');

-- ── System Wildcard ──────────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'SYSTEM:ALL:ADMIN', 'system', 'ADMIN', 'Full admin access to all resources — SUPER_ADMIN only');


-- ============================================================
-- 7. SEED — ROLE ↔ PERMISSION MAPPINGS
-- ============================================================

-- ── SUPER_ADMIN → every permission ──────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'), permission_id
FROM permissions;

-- ── ADMIN ────────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'CUSTOMER_SVC:PROFILE:WRITE:ANY',
    'CUSTOMER_SVC:KYC:APPROVE',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY'
);

-- ── BRANCH_MGR ───────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'BRANCH_MGR'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'CUSTOMER_SVC:PROFILE:WRITE:ANY',
    'CUSTOMER_SVC:KYC:APPROVE',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN',
    'ACCOUNT_SVC:ACCOUNT:APPROVE:CLOSE',
    'ACCOUNT_SVC:ACCOUNT:APPROVE:FREEZE',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY'
);

-- ── RM ───────────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'RM'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ASSIGNED',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN_ON_BEHALF',
    'ACCOUNT_SVC:ACCOUNT:READ:ASSIGNED'
);

-- ── TELLER ───────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'TELLER'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN'
);

-- ── AUDITOR ──────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'AUDITOR'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY',
    'TRANSACTION_SVC:TRANSACTION:READ:OWN'
);

-- ── FRAUD_ANALYST ────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'FRAUD_ANALYST'), permission_id
FROM permissions
WHERE permission_code IN (
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:APPROVE:FREEZE',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY'
);

-- ── CUSTOMER (base marker — no permissions) ──────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'CUSTOMER'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN'
);

-- ── CUSTOMER_REGISTERED ─────────────────────────────────────
-- Stage 1: registered, KYC not started.
-- Can view and update own profile only.
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'CUSTOMER_REGISTERED'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN',
    'CUSTOMER_SVC:KYC:READ:OWN',
    'CUSTOMER_SVC:KYC:WRITE:OWN'
);


-- ── CUSTOMER_ACTIVE ─────────────────────────────────────────
-- Stage 2: KYC approved, full banking access unlocked.
-- All previous permissions + account and transaction access.
INSERT INTO role_permissions (role_id, permission_id)
SELECT (SELECT role_id FROM roles WHERE role_name = 'CUSTOMER_ACTIVE'), permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN',
    'CUSTOMER_SVC:KYC:READ:OWN',
    'CUSTOMER_SVC:KYC:WRITE:OWN',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN',
    'TRANSACTION_SVC:TRANSACTION:READ:OWN',
    'TRANSACTION_SVC:TRANSACTION:CREATE:OWN'
);


-- ============================================================
-- 8. SEED — ASSIGN SUPER_ADMIN TO SYSTEM USER
-- ============================================================
INSERT INTO user_roles (user_id, role_id, assigned_by, assigned_at)
SELECT
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    NOW();

-- Seed the history record for the system user's initial role assignment
INSERT INTO user_role_history (user_id, role_id, action, reason, changed_by, changed_at)
SELECT
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
    'ASSIGNED',
    'MANUAL_ADMIN_GRANT',
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    NOW();

-- ============================================================
-- END OF V1
-- ============================================================
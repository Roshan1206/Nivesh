-- ============================================================
-- Nivesh Bank — Auth Service Seed Data
-- ============================================================

-- ============================================================
-- 1. SYSTEM USER
-- ============================================================
INSERT INTO users (
    user_id,
    mobile_number,
    email,
    password,
    failed_attempt,
    locked_until,
    customer_status,
    is_kyc_verified,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES (
    gen_random_uuid(),
    '0000000000',
    'system@nvsh.com',
    '$2a$12$bVnFPMbHqjIv.WNIajOJxOzFqgP/YD9ANy9pJnm1e/y1v6B1tV2ge', -- BCrypt of Password@1234
    0,
    NULL,
    'ACTIVE',
    TRUE,
    NOW(),
    'system',
    NOW(),
    'system'
);

-- ============================================================
-- 2. ROLES
-- ============================================================
INSERT INTO roles (role_id, role_name, description, is_system_role, created_at, created_by, updated_at, updated_by) VALUES
    (gen_random_uuid(), 'SUPER_ADMIN',   'Super administrator with all permissions',               TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'ADMIN',         'Bank administrator for customer & account management',   TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'BRANCH_MGR',    'Branch manager with operational authority',              TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'RM',            'Relationship manager handling assigned customers',       TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'TELLER',        'Bank teller for front-desk customer operations',         TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'AUDITOR',       'Internal auditor with read-only access across services', TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'FRAUD_ANALYST', 'Fraud analyst for account monitoring and freezing',      TRUE,  NOW(), 'system', NOW(), 'system'),
    (gen_random_uuid(), 'CUSTOMER',      'End customer with access to own data only',              FALSE, NOW(), 'system', NOW(), 'system');

-- ============================================================
-- 3. PERMISSIONS
-- ============================================================

-- ── 3.1 Customer Service ────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:OWN',      'customer-service', 'READ',    'View own customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:WRITE:OWN',     'customer-service', 'WRITE',   'Update own customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:ANY',      'customer-service', 'READ',    'View any customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:READ:ASSIGNED', 'customer-service', 'READ',    'View profiles of assigned customers (RM only)'),
    (gen_random_uuid(), 'CUSTOMER_SVC:PROFILE:WRITE:ANY',     'customer-service', 'WRITE',   'Update any customer profile'),
    (gen_random_uuid(), 'CUSTOMER_SVC:KYC:APPROVE',           'customer-service', 'APPROVE', 'Verify and approve customer KYC');

-- ── 3.2 Account Service ─────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:OWN',                   'account-service', 'READ',    'View own bank accounts'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN',                 'account-service', 'WRITE',   'Open a new bank account for self'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN_ON_BEHALF',       'account-service', 'WRITE',   'Open a bank account on behalf of an assigned customer (RM only)'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:APPROVE:CLOSE',              'account-service', 'APPROVE', 'Close a customer bank account'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:APPROVE:FREEZE',             'account-service', 'APPROVE', 'Freeze a customer bank account'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:ANY',                   'account-service', 'READ',    'View all bank accounts across customers'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:READ:ASSIGNED',              'account-service', 'READ',    'View accounts of assigned customers (RM only)'),
    (gen_random_uuid(), 'ACCOUNT_SVC:ACCOUNT:WRITE:INTERNAL_DEBIT_CREDIT','account-service', 'WRITE',   'Internal debit/credit — service-to-service only, no user role');

-- ── 3.3 System wildcard ──────────────────────────────────────
INSERT INTO permissions (permission_id, permission_code, resource, action_granted, description) VALUES
    (gen_random_uuid(), 'SYSTEM:ALL:ADMIN', 'system', 'ADMIN', 'Full admin access to all resources — SUPER_ADMIN only');

-- ============================================================
-- 4. ROLE ↔ PERMISSION MAPPINGS  (query by name)
-- ============================================================

-- ── SUPER_ADMIN → all permissions ───────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
    permission_id
FROM permissions;

-- ── ADMIN ────────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'ADMIN'),
    permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'CUSTOMER_SVC:PROFILE:WRITE:ANY',
    'CUSTOMER_SVC:KYC:APPROVE',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN'
);

-- ── BRANCH_MGR ───────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'BRANCH_MGR'),
    permission_id
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
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'RM'),
    permission_id
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
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'TELLER'),
    permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN'
);

-- ── AUDITOR ──────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'AUDITOR'),
    permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:READ:ANY',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY'
);

-- ── FRAUD_ANALYST ────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'FRAUD_ANALYST'),
    permission_id
FROM permissions
WHERE permission_code IN (
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:APPROVE:FREEZE',
    'ACCOUNT_SVC:ACCOUNT:READ:ANY'
);

-- ── CUSTOMER ─────────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT role_id FROM roles WHERE role_name = 'CUSTOMER'),
    permission_id
FROM permissions
WHERE permission_code IN (
    'CUSTOMER_SVC:PROFILE:READ:OWN',
    'CUSTOMER_SVC:PROFILE:WRITE:OWN',
    'ACCOUNT_SVC:ACCOUNT:READ:OWN',
    'ACCOUNT_SVC:ACCOUNT:WRITE:OPEN'
);

-- ============================================================
-- 5. ASSIGN SUPER_ADMIN ROLE TO SYSTEM USER
-- ============================================================
INSERT INTO user_roles (user_id, role_id, assigned_by, assigned_at)
SELECT
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    (SELECT role_id FROM roles WHERE role_name = 'SUPER_ADMIN'),
    (SELECT user_id FROM users WHERE email     = 'system@nvsh.com'),
    NOW();

-- ============================================================
-- END OF SEED
-- ============================================================
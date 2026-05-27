# Nivesh Bank — Roles & Permissions Design


## 1. Roles Overview

| Role | Who | Description |
|---|---|---|
| `GUEST` | Unauthenticated / PENDING_KYC users | Register, login, submit KYC only |
| `CUSTOMER` | Verified retail banking customer | Own accounts, transactions, loans, cards |
| `RELATIONSHIP_MANAGER` | Bank staff managing customers | View/assist assigned customers |
| `BRANCH_MANAGER` | Branch head | Approve loans, FDs, override limits |
| `TELLER` | Counter staff | Deposits, withdrawals, basic queries |
| `FRAUD_ANALYST` | Risk/ops team | View fraud flags, freeze accounts |
| `AUDITOR` | Compliance/audit team | Read-only access to everything |
| `ADMIN` | System administrator | User management, config, system health |
| `INTERNAL_SERVICE` | Service-to-service calls only | Internal APIs (debit/credit, balance) |
| `SUPER_ADMIN` | Highest privilege | Everything + role assignment |

---

## 2. Permission Matrix

### 2.1 Customer Service

| Permission | CUSTOMER | RM | TELLER | BRANCH_MGR | AUDITOR | ADMIN |
|---|---|---|---|---|---|---|
| View own profile | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update own profile | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| View any customer | ❌ | ✅ (assigned) | ✅ | ✅ | ✅ | ✅ |
| Update any customer | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |
| Verify KYC | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |

### 2.2 Account Service

| Permission | CUSTOMER | RM | TELLER | BRANCH_MGR | AUDITOR | FRAUD_ANALYST |
|---|---|---|---|---|---|---|
| View own accounts | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Open account | ✅ | ✅ (on behalf) | ❌ | ✅ | ❌ | ❌ |
| Close account | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Freeze account | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |
| View all accounts | ❌ | ✅ (assigned) | ❌ | ✅ | ✅ | ✅ |
| Internal debit/credit | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

> Internal debit/credit → `INTERNAL_SERVICE` role only. Never exposed to any human role.

### 2.3 Transaction Service

| Permission | CUSTOMER | TELLER | BRANCH_MGR | AUDITOR | FRAUD_ANALYST |
|---|---|---|---|---|---|
| Initiate transfer | ✅ | ✅ | ✅ | ❌ | ❌ |
| View own transactions | ✅ | ✅ | ✅ | ✅ | ✅ |
| View any transaction | ❌ | ❌ | ✅ | ✅ | ✅ |
| Reverse transaction | ❌ | ❌ | ✅ | ❌ | ❌ |
| Export statement | ✅ (own) | ✅ | ✅ | ✅ | ✅ |

### 2.4 Loans & Cards

| Permission | CUSTOMER | RM | BRANCH_MGR | AUDITOR |
|---|---|---|---|---|
| Apply for loan/card | ✅ | ✅ (on behalf) | ✅ | ❌ |
| View own loan/card | ✅ | ✅ | ✅ | ✅ |
| Approve loan | ❌ | ❌ | ✅ | ❌ |
| Reject loan | ❌ | ❌ | ✅ | ❌ |
| Set credit limit | ❌ | ❌ | ✅ | ❌ |

### 2.5 Fraud & Audit Service

| Permission | FRAUD_ANALYST | BRANCH_MGR | AUDITOR | ADMIN |
|---|---|---|---|---|
| View fraud alerts | ✅ | ✅ | ✅ | ✅ |
| Resolve fraud case | ✅ | ❌ | ❌ | ✅ |
| View audit logs | ❌ | ❌ | ✅ | ✅ |
| Export audit report | ❌ | ❌ | ✅ | ✅ |

---

## 3. JWT Claims Design

```json
{
  "sub": "uuid-of-user",
  "cifNumber": "00000042",
  "roles": ["CUSTOMER"],
  "permissions": [
    "account:read:own",
    "transaction:write:own",
    "loan:read:own"
  ],
  "kycStatus": "VERIFIED",
  "status": "ACTIVE",
  "branchCode": null,
  "iat": 1234567890,
  "exp": 1234571490
}
```

| Claim | Purpose |
|---|---|
| `roles` | Coarse-grained, used for `hasRole()` checks |
| `permissions` | Fine-grained, used for `hasAuthority()` method-level security |
| `branchCode` | Present for TELLER/BRANCH_MGR; scopes access to their branch only |
| `kycStatus` | Guards account opening without a DB call |
| `status` | Blocks PENDING_KYC users from transactional APIs |

---

## 4. Spring Security Examples

```java
// Coarse role check
@PreAuthorize("hasRole('BRANCH_MANAGER')")
public LoanResponse approveLoan(String loanId) { }

// Fine-grained permission check
@PreAuthorize("hasAuthority('account:read:own')")
public AccountResponse getMyAccount(String accountId) { }

// Own-resource guard (userId from JWT must match resource owner)
@PreAuthorize("hasAuthority('transaction:read:own') " +
              "and @resourceGuard.isOwner(authentication, #accountId)")
public List<Transaction> getTransactions(String accountId) { }

// Internal service only — never callable by human roles
@PreAuthorize("hasRole('INTERNAL_SERVICE')")
public void debitBalance(UUID accountId, BigDecimal amount) { }
```

---

## 5. Key Design Rules

1. **`INTERNAL_SERVICE` is never assigned to a human** — only service accounts use it, authenticated via mTLS + Vault secret, not username/password.
2. **CUSTOMER can only access own resources** — enforced both via JWT permissions (`:own` suffix) and a `resourceGuard` bean that verifies ownership.
3. **AUDITOR is strictly read-only** — no write permission anywhere, ever.
4. **TELLER is branch-scoped** — `branchCode` in JWT limits queries to their branch's customers only.
5. **SUPER_ADMIN never logs in from the app** — only accessible via internal admin console; all actions written to audit log.
6. **Auth Service never calls Customer Service** — registration is two explicit client-driven steps; no internal coupling.
7. **Balance is owned exclusively by Account Service** — `/internal/accounts/{id}/debit` and `/credit` are called only by Transaction Service within the cluster.
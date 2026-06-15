# 🏦 Nivesh Bank — Microservices Banking Platform

A production-grade, cloud-native banking backend built with Java 21 and Spring Boot 3.5. Nivesh Bank simulates the core operations of a modern retail bank — authentication, customer onboarding, account management, and fund transfers — across independently deployable microservices connected through an API Gateway, Kafka event bus, and Spring Cloud Config.

---

## Architecture Overview

```
                        ┌─────────────────────────────────────────────────────────┐
                        │                   API Gateway  :8080                    │
                        │   JWT validation · Route filtering · Header sanitization │
                        └──────────────┬──────────────────────────────────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          ▼                            ▼                            ▼
  Auth Service :8081         Customer Service :8082       Account Service :8083
  OAuth2 Auth Server         KYC · Registration           Balance · Debit · Credit
  JWT issuance               CIF number generation        Luhn account numbers
  Refresh tokens             Aadhaar/PAN verification     Saga compensation
  Role & permission RBAC     Contact management           Optimistic locking

          └────────────────────────────┬────────────────────────────┘
                                       ▼
                           Transaction Service :8084
                           OTP-gated transfers · Choreography Saga
                           Idempotency · Retry scheduler
                           Journal entries · GL accounts

          ┌────────────────────────────┬────────────────────────────┐
          ▼                            ▼                            ▼
  Config Server :8888       Eureka Registry :8761        Kafka + Redis
  Git-backed config          Service discovery            Event bus · Cache
  RSA key distribution       Load balancing               Token blacklist
```

---

## Services

| Service | Port | Description |
|---|---|---|
| `gateway` | 8080 | Spring Cloud Gateway — JWT auth, routing, header sanitization |
| `authentication` | 8081 | OAuth2 Authorization Server — JWT issuance, refresh tokens, RBAC |
| `customer` | 8082 | Customer registration, KYC initiation/verification |
| `account` | 8083 | Account lifecycle, balance management, Saga compensation |
| `transaction` | 8084 | OTP-gated fund transfers, choreography Saga, retry scheduler |
| `config` | 8888 | Spring Cloud Config Server — Git-backed configuration |
| `eureka` | 8761 | Netflix Eureka — service discovery and load balancing |
| `library` | — | Shared library (`nivesh-lib`) — JWT, OTP, auditing, Kafka events |

---

## Tech Stack

**Core:** Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Spring Cloud 2025.0

**Messaging:** Apache Kafka — Choreography Saga pattern (compensate-request/success/failed topics)

**Databases:** PostgreSQL 16 (schema-per-service isolation), Flyway migrations, optimistic locking

**Caching:** Redis — JWT blacklist, OTP cache, token version tracking, idempotency records

**Service Mesh:** Spring Cloud Gateway, Eureka, Spring Cloud Config (Git-backed)

**Security:** RSA key-pair JWT (RS256), BCrypt password encoding, OTP-gated transactions, token versioning for logout-all

**Observability:** Spring Boot Actuator, structured logging at TRACE level

**Build:** Gradle multi-module, `mavenLocal()` for shared library

---

## Key Engineering Decisions

### Account Numbers — 11-digit Luhn-protected
```
Digit 1     : Product prefix (1=Savings, 2=Current, 3=Salary, 4=Premium)
Digits 2–10 : Zero-padded PostgreSQL sequence (per product, max 99,999,999)
Digit 11    : Luhn check digit computed at runtime
```

### Customer Numbers — 8-digit zero-padded CIF
Internal UUIDs stay internal. All external APIs reference the 8-character CIF number (e.g. `00000042`).

### IFSC Code Format
`NVSH0` + `PIN_ZONE (3 digits)` + `BRANCH_SEQ (3 digits)` = 11 characters. One branch per 5 km radius via PostGIS (planned).

### Choreography Saga — Distributed Transactions
```
Transaction Service                  Account Service
      │                                    │
      ├─── debit(sourceAccount) ──────────►│
      │◄── DEBIT_SUCCESS ─────────────────┤
      │                                    │
      ├─── credit(destinationAccount) ────►│
      │                                    │
      │   [Credit fails]                   │
      │◄── CREDIT_RETRY ──────────────────┤
      │                                    │
      ├─── Kafka: compensate.request ─────►│
      │◄── Kafka: compensate.success ──────┤
      │                                    │
      └─── status: REVERSED ───────────────┘
```

Dead-letter topic (`transaction.dead.letter`) captures compensation failures after `compensateRetryCount` is exhausted. Status transitions to `MANUAL_REVIEW`.

### Idempotency
Every debit/credit operation requires an `idempotencyKey` header. Results are persisted in `idempotency_records` (TTL: 24 hours) so duplicate requests replay the original response without re-executing the transaction.

### Optimistic Locking on Accounts
The `accounts` table carries a `version` column. Concurrent balance updates trigger `ObjectOptimisticLockingFailureException`, which returns HTTP 429 (Too Many Requests) to the caller rather than silently corrupting balance.

### OTP-Gated Transactions
Every fund transfer requires a 6-digit OTP verified before money moves. OTPs are stored in Redis with a 5-minute TTL, maximum 3 attempts, and evicted on success or exhaustion.

### Role Hierarchy & Per-User Permission Overrides
```
SUPER_ADMIN → ADMIN → BRANCH_MGR → TELLER
CUSTOMER_ACTIVE → CUSTOMER_REGISTERED → CUSTOMER
```
Permissions follow the format `SERVICE:RESOURCE:ACTION[:SCOPE]` (e.g. `ACCOUNT_SVC:ACCOUNT:WRITE:OPEN`). Each user can receive `GRANT` or `REVOKE` overrides on top of their role defaults, with optional expiry timestamps.

### Token Versioning — Logout All Sessions
Incrementing `token_version` in the `users` table and publishing it to Redis invalidates all previously issued tokens for that user simultaneously, without requiring a database lookup per request.

---

## Project Structure

```
nivesh-bank/
├── gateway/              # Spring Cloud Gateway
├── authentication/       # OAuth2 Authorization Server
├── customer/             # Customer & KYC service
├── account/              # Account & balance service
├── transaction/          # Transaction orchestration
├── config/               # Spring Cloud Config Server
├── eureka/               # Service registry
└── library/              # Shared library (nivesh-lib)
    ├── configuration/    # Security, cache, Kafka, audit configs
    ├── service/          # JWT, OTP, sequence generator
    ├── dto/              # Shared request/response/event DTOs
    ├── entity/           # BaseAudit, shared enums
    └── util/             # LuhnUtil, validation annotations
```

---

## Database Design

Each service owns its PostgreSQL schema (schema-per-service isolation):

| Schema | Service | Key Tables |
|---|---|---|
| `auth` | Authentication | `users`, `roles`, `permissions`, `refresh_tokens`, `user_permission_overrides` |
| `customer` | Customer | `customers`, `contacts`, `addresses`, `kyc_documents`, `nominees` |
| `account` | Account | `accounts`, `products`, `fixed_deposits`, `recurring_deposits`, `idempotency_records` |
| `txn` | Transaction | `transactions`, `journal_entries`, `gl_accounts`, `transaction_type_configs`, `standing_instructions` |

The `txn` schema enforces **double-entry bookkeeping** via a PostgreSQL trigger on `journal_entries`: every posted transaction must have exactly one DR and one CR row with equal amounts.

---

## Running Locally

### Prerequisites
- Java 21
- PostgreSQL 16
- Redis
- Apache Kafka
- Gradle 8+

### 1. Start infrastructure
```bash
# PostgreSQL, Redis, Kafka via Docker Compose (add your own compose file)
docker-compose up -d
```

### 2. Create database schemas
```sql
CREATE SCHEMA auth;
CREATE SCHEMA customer;
CREATE SCHEMA account;
CREATE SCHEMA txn;
```

### 3. Publish shared library to mavenLocal
```bash
cd library
./gradlew publishToMavenLocal
```

### 4. Start services in order
```bash
# 1. Config Server
cd config && ./gradlew bootRun

# 2. Eureka
cd eureka && ./gradlew bootRun

# 3. Authentication
cd authentication && ./gradlew bootRun

# 4. Customer, Account, Transaction (any order)
cd customer && ./gradlew bootRun
cd account && ./gradlew bootRun
cd transaction && ./gradlew bootRun

# 5. Gateway (last)
cd gateway && ./gradlew bootRun
```

### 5. RSA Keys
Place your RSA key pair at:
```
authentication/src/main/resources/keys/private.pem
authentication/src/main/resources/keys/public.pem
```
Generate with:
```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

---

## API Flow — End to End

### Register → KYC → Transfer

```
1. POST /auth/register             → OTP sent to email
2. POST /auth/register/verify/{id} → Returns ONBOARDED access token

3. POST /customers                 → Register customer profile
                                     Returns REGISTERED access token

4. POST /kyc                       → Submit document, OTP sent
5. POST /kyc/verify/{id}           → Verify OTP → ACTIVE access token

6. POST /accounts                  → Open bank account (product code: 001)

7. POST /transactions              → Initiate transfer, OTP sent
   Header: idempotencyKey: <uuid>

8. POST /transactions/{id}/verify  → Submit OTP → funds move
```

---

## Security Architecture

All traffic enters through the API Gateway, which validates JWT signatures against the Auth service's JWK endpoint. Internal service-to-service calls bypass end-user JWT checks using a shared header:

```
X-Internal-Role: INTERNAL_SERVICE
X-Source-Service: <service-name>
```

Internal endpoints (`/*/internal/**`) are protected by `InternalServiceAuthorizationManager`, which verifies both headers rather than user roles. The Gateway's `SecurityHeaderFilter` strips these headers from external requests, preventing injection attacks.

---

## Configuration (Spring Cloud Config)

Runtime configuration is served from a Git repository (`nivesh-config`) via the Config Server at port 8888. Sensitive values (mail credentials, token expiry, Kafka bootstrap servers) are environment-variable interpolated:

```yaml
spring.mail.username: ${MAIL_ID:fallback@example.com}
spring.mail.password: ${MAIL_PWD:changeme}
```

---

## Planned Enhancements

- Payments Service (8085) — UPI, NEFT, RTGS, IMPS rails
- Loans Service (8086)
- Cards Service (8087)
- Notifications Service (8088) — Kafka-driven email/SMS
- Fraud Service (8089) — real-time scoring
- Cassandra for transaction history older than 90 days
- HashiCorp Vault for secrets management
- Distributed tracing (Micrometer + Zipkin)
- Docker Compose + Kubernetes manifests

---

## Author

**Roshan Lal Sahu** — Senior Software Engineer  
[LinkedIn](https://linkedin.com/in/roshansahu96) · [GitHub](https://github.com/Roshan1206)

---

> Built to demonstrate production-grade microservices architecture patterns: Saga orchestration, idempotency, optimistic concurrency, RBAC with permission overrides, distributed caching, and event-driven compensation flows.

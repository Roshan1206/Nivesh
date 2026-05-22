# Nivesh
Nivesh is a banking project with features replicating a real world bank

Architecture Philosophy: The platform follows a Domain-Driven Design (DDD) approach where each bounded
context maps to an independent microservice. Services communicate asynchronously via Apache Kafka for
high-throughput operations and synchronously via REST for real-time queries. The entire platform will run
on Kubernetes.

The attached PDF contains the very first design. It has been modified multiple times during the implementation.


## 🏦 Microservices Architecture Overview
Here's the complete architecture document. Here's a summary of what's inside:

| #  | Service Name              | Core Responsibility |
|----|---------------------------|---------------------|
| 01 | API Gateway               | Rate limiting, routing, JWT pre-validation, IP blocking |
| 02 | Identity & Auth           | OAuth2, MFA (OTP/TOTP/Biometric), RBAC, session management |
| 03 | Customer Profile          | KYC (Aadhaar/PAN/Video KYC), nominees, addresses, segmentation |
| 04 | Account Management        | Savings, FD, RD, Jan Dhan, NRI accounts, interest accrual |
| 05 | Transaction Processing    | Double-entry ledger, ATM, cheque, standing instructions |
| 06 | Payments & Fund Transfer  | UPI, NEFT, RTGS, IMPS, NACH, BBPS, SWIFT |
| 07 | Loan & Credit             | Home/Personal/Auto loans, EMI, NPA, credit scoring |
| 08 | Card Management           | Debit/Credit/Virtual cards, PCI-DSS, rewards, chargebacks |
| 09 | Notification              | SMS, Email, Push, WhatsApp, in-app alerts |
| 10 | Fraud Detection           | Real-time risk scoring (<100ms), ML model, AML rules |
| 11 | Reporting & Statements    | PDFs, tax certificates, RBI regulatory returns |
| 12 | Audit & Compliance        | Immutable hash-chain audit, STR filing to FIU-IND |
| 13 | Investment & Wealth       | Mutual Funds, SGB, NPS, Insurance distribution |
| 14 | Branch & ATM              | Geospatial search, ATM cash monitoring, appointments |

## Library
A common library has been included for shared resources. Run the below script to build and publish the jar.
```shell
    cd library; ./gradlew clean build publishToMavenLocal
```

## Auth
Public and Private keys are required for the auth module to run.
Run the below script to generate and move it to correct location.
```shell
    openssl genrsa -out private.pem 2048;
    openssl rsa -in private.pem -pubout -out public.pem
    mv private.pem authentication/src/main/resources/keys/
    mv public.pem  authentication/src/main/resources/keys/
```
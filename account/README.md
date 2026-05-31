# Account Management Service

Manages all bank account types: Savings, Current, FD, RD, NRI (NRE/NRO/FCNR), Salary, Jan Dhan, and PPF. Owns the canonical
balance — the authoritative source of truth. Transaction Service always calls internal debit/credit APIs rather than writing to accounts
directly

product_code | product_name     | account_prefix
-------------|------------------|---------------
001          | Savings Account  | 1
002          | Current Account  | 2
003          | Fixed Deposit    | 3
004          | Recurring Deposit| 4
005          | NRE Account      | 5
006          | NRO Account      | 6
007          | FCNR Account     | 7
008          | Salary Account   | 8
009          | Jan Dhan Account | 9
package com.nivesh.transaction.entity.enums;

public enum TransactionStatus {
    INITIATED,
    DEBIT_SUCCESS,
    FRAUD_CHECK,
    PENDING,
    POSTED,
    FAILED,
    REVERSED,
    BLOCKED,
    COMPENSATE_INITIATED
}

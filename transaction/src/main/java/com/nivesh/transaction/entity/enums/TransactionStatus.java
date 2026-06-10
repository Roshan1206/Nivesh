package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported transaction status values used by the transaction domain model.
 */
public enum TransactionStatus {
    /** Transaction has been created but not yet processed. */
    INITIATED,

    /** Source account debit completed successfully. */
    DEBIT_SUCCESS,

    /** Transaction is undergoing fraud screening. */
    FRAUD_CHECK,

    /** Transaction is waiting for a downstream step or confirmation. */
    PENDING,

    /** Transaction has been posted to the ledger. */
    POSTED,

    /** Transaction failed before completion. */
    FAILED,

    /** Transaction has been reversed. */
    REVERSED,

    /** Transaction is blocked by business, fraud, or compliance controls. */
    BLOCKED,

    /** Compensating action has started for a failed or reversed flow. */
    COMPENSATE_INITIATED,

    /** Compensation Exhausted */
    MANUAL_REVIEW,

    CREDIT_RETRY
}

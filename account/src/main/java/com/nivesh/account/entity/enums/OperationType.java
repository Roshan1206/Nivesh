package com.nivesh.account.entity.enums;

/**
 * Enumerates supported operation type values used by the account domain model.
 */
public enum OperationType {

    /** Money is withdrawn from or charged to an account. */
    DEBIT,

    /** Money is deposited into or added to an account. */
    CREDIT
}

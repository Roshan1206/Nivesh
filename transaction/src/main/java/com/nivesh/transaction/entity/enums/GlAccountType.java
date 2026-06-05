package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported GL account type values used by the transaction domain model.
 */
public enum GlAccountType {

    /** Ledger account that represents bank assets. */
    ASSET,

    /** Ledger account that represents bank liabilities. */
    LIABILITY,

    /** Ledger account that records income. */
    INCOME,

    /** Ledger account that records expenses. */
    EXPENSE
}

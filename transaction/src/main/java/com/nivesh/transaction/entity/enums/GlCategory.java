package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported GL category values used by the transaction domain model.
 */
public enum GlCategory {
    /** General ledger entry is associated with a customer-facing account. */
    CUSTOMER,

    /** General ledger entry is associated with an internal bank ledger. */
    INTERNAL_GL
}

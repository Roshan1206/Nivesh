package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported cheque status values used by the transaction domain model.
 */
public enum ChequeStatus {

    /** Cheque has been presented for processing. */
    PRESENTED,

    /** Cheque is currently moving through clearing. */
    IN_CLEARING,

    /** Cheque has cleared successfully. */
    CLEARED,

    /** Cheque was rejected due to insufficient funds or validation issues. */
    BOUNCED,

    /** Cheque was returned without successful clearing. */
    RETURNED
}

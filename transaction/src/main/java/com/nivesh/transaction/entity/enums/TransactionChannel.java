package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported transaction channel values used by the transaction domain model.
 */
public enum TransactionChannel {

    /** Transaction originated from an automated teller machine. */
    ATM,

    /** Transaction originated from Unified Payments Interface rails. */
    UPI,

    /** Transaction originated from NEFT payment rails. */
    NEFT,

    /** Transaction originated from RTGS payment rails. */
    RTGS,

    /** Transaction originated from IMPS payment rails. */
    IMPS,

    /** Transaction originated at a branch. */
    BRANCH,

    /** Transaction originated from mobile banking. */
    MOBILE,

    /** Transaction originated from internet banking. */
    INTERNET_BANKING,

    /** Transaction originated from a point-of-sale terminal. */
    POS,

    /** Transaction originated from an API integration. */
    API
}

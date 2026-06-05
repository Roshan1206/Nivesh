package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported ATM reject reason values used by the transaction domain model.
 */
public enum AtmRejectReason {
    /** ATM does not have enough cash to dispense. */
    INSUFFICIENT_CASH,

    /** Card is blocked and cannot be used. */
    CARD_BLOCKED,

    /** Requested withdrawal exceeds the daily limit. */
    DAILY_LIMIT_EXCEEDED,

    /** Submitted card PIN is incorrect. */
    PIN_INCORRECT,

    /** Account balance is insufficient for the ATM transaction. */
    INSUFFICIENT_BALANCE,

    /** ATM transaction failed because of a technical issue. */
    TECHNICAL_FAILURE
}

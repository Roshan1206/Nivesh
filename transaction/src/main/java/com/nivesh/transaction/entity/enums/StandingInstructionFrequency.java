package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported standing instruction frequency values used by the transaction domain model.
 */
public enum StandingInstructionFrequency {
    /** Instruction runs every day. */
    DAILY,

    /** Instruction runs once each week. */
    WEEKLY,

    /** Instruction runs once each month. */
    MONTHLY,

    /** Instruction runs once each quarter. */
    QUARTERLY,

    /** Instruction runs once each year. */
    YEARLY
}

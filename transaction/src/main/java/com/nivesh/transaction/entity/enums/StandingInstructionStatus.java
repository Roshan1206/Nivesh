package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported standing instruction status values used by the transaction domain model.
 */
public enum StandingInstructionStatus {

    /** Standing instruction is active and eligible to run. */
    ACTIVE,

    /** Standing instruction is temporarily suspended. */
    PAUSED,

    /** Standing instruction has been cancelled. */
    CANCELLED,

    /** Standing instruction has finished its configured schedule. */
    COMPLETED,

    /** Standing instruction failed during execution. */
    FAILED
}

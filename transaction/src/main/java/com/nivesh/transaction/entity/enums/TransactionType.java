package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported transaction type values used by the transaction domain model.
 */
public enum TransactionType {

    /**
     * Customer initiates outbound payment
     */
    DEBIT,

    /**
     *  Money arrives into customer account
     */
    CREDIT,

    /**
     * Undoing a prev POSTED transaction
     */
    REVERSAL,

    /**
     * Service charge levied by bank
     */
    FEE,

    /**
     * Interest credited to savings/FD/RD accounts
     */
    INTEREST,

    /**
     * Money returned to customer
     */
    REFUND,

    /**
     * Auto-debit from Standing Instruction batch job
     */
    STANDING_INSTRUCTION,

    /**
     * Fixed Deposit maturity payout
     */
    FD_MATURITY,

    /**
     * Recurring Deposit maturity payout
     */
    RD_MATURITY,

    /** Funds move between customer or internal accounts. */
    TRANSFER
}

package com.nivesh.account.entity.enums;

/**
 * Define Account status
 *
 * @author Roshan
 */
public enum Status {

    /** Account is active and eligible for standard operations. */
    ACTIVE,

    /** Account is temporarily restricted from transactional activity. */
    FROZEN,

    /** Account has been inactive long enough to require reactivation checks. */
    DORMANT,

    /** Account has been permanently closed. */
    CLOSED,

    /** Account is awaiting completion of required setup or approval steps. */
    PENDING,

    /** Account is in the process of being activated. */
    ACTIVATION,

    /** Deposit account was closed before its maturity date. */
    PREMATURELY_CLOSED,

    /** Deposit account has reached its maturity date. */
    MATURED,

    /** Deposit account has been renewed for another term. */
    RENEWED
}

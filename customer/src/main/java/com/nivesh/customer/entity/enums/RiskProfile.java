package com.nivesh.customer.entity.enums;

/**
 * Enumerates supported risk profile values used by the customer domain model.
 */
public enum RiskProfile {

    /** Customer has a low risk classification. */
    LOW,

    /** Customer has a moderate risk classification. */
    MEDIUM,

    /** Customer has a high risk classification. */
    HIGH,

    /** Customer has a very high risk classification. */
    VERY_HIGH,

    /** Customer is blocked due to risk or compliance concerns. */
    BLOCKED
}

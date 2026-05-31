package com.nivesh.library.entity.enums;

/**
 * Represents the lifecycle status of a customer account.
 * Determines token generation eligibility and access permissions.
 *
 * @author Roshan
 */
public enum CustomerStatus {

    /** Customer has completed onboarding process */
    ONBOARDED,

    /** Customer account is fully active and usable */
    ACTIVE,

    /** Account is temporarily locked due to security concerns */
    LOCKED,

    /** Customer has registered but not completed onboarding */
    REGISTERED,

    /** Account has been permanently deactivated */
    DEACTIVATED;

    /**
     * Checks if this status equals the provided status.
     *
     * @param status the status to compare with
     * @return true if statuses are identical
     */
    public boolean isEqual(CustomerStatus status) {
        return this == status;
    }

}

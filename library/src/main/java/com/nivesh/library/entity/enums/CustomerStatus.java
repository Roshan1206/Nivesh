package com.nivesh.library.entity.enums;

/**
 * User status. Used for generating valid token
 *
 * @author Roshan
 */
public enum CustomerStatus {

    ONBOARDED,
    ACTIVE,
    LOCKED,
    REGISTERED,
    DEACTIVATED;

    /**
     * Validates user status
     */
    public boolean isEqual(CustomerStatus status) {
        return this == status;
    }

}

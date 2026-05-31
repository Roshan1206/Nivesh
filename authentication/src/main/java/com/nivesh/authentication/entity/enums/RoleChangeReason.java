package com.nivesh.authentication.entity.enums;

/**
 * Represent the business reason that triggered role change.
 */
public enum RoleChangeReason {
    REGISTRATION_COMPLETE,
    KYC_SUBMITTED,
    KYC_APPROVED,
    KYC_REJECTED,
    MANUAL_ADMIN_GRANT,
    MANUAL_ADMIN_REVOKE,
    ACCOUNT_LOCKED,
    ACCOUNT_DEACTIVATE
}

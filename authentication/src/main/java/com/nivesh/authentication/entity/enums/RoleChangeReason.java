package com.nivesh.authentication.entity.enums;

/**
 * Represent the business reason that triggered role change.
 */
public enum RoleChangeReason {
    /** Role changed after registration was completed. */
    REGISTRATION_COMPLETE,

    /** Role changed after KYC details were submitted. */
    KYC_SUBMITTED,

    /** Role changed after KYC verification was approved. */
    KYC_APPROVED,

    /** Role changed after KYC verification was rejected. */
    KYC_REJECTED,

    /** Role was manually granted by an administrator. */
    MANUAL_ADMIN_GRANT,

    /** Role was manually revoked by an administrator. */
    MANUAL_ADMIN_REVOKE,

    /** Role changed because the account was locked. */
    ACCOUNT_LOCKED,

    /** Role changed because the account was deactivated. */
    ACCOUNT_DEACTIVATE
}

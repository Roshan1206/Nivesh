package com.nivesh.library.entity.enums;

/**
 * Defines the purpose or use case for an OTP.
 */
public enum OtpPurpose {

    /** OTP for initial user registration */
    USER_REGISTRATION,

    /** OTP for KYC (Know Your Customer) verification */
    KYC_VERIFICATION,

    /** OTP for high-value transaction authorization */
    TRANSACTION,

    LOGIN
}

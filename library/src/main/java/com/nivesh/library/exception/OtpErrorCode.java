package com.nivesh.library.exception;

/**
 * Error codes for OTP validation failures.
 */
public enum OtpErrorCode {
    /** OTP has expired and cannot be validated */
    EXPIRED,

    /** Submitted OTP does not match the generated one */
    INVALID,

    /** Maximum validation attempts have been exceeded */
    MAX_ATTEMPTS_EXCEEDED
}

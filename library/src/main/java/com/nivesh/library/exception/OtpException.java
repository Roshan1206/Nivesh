package com.nivesh.library.exception;

/**
 * Exception raised when OTP conditions occur.
 */
public class OtpException extends RuntimeException {

    /** Typed OTP error code used to select the response status. */
    private final OtpErrorCode errorCode;

    /**
     * Creates an OTP exception with a message and typed error code.
     */
    public OtpException(String message, OtpErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OtpErrorCode getErrorCode() {
        return errorCode;
    }
}

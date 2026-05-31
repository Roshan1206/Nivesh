package com.nivesh.library.exception;

public class OtpException extends RuntimeException {

    private final OtpErrorCode errorCode;

    public OtpException(String message, OtpErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OtpErrorCode getErrorCode() {
        return errorCode;
    }
}

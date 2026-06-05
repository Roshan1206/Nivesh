package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception raised when account already exists conditions occur.
 */
@Getter
public class AccountAlreadyExistsException extends RuntimeException {

    /** HTTP status that should be returned to the caller. */
    private final HttpStatus status;

    /**
     * Creates an exception for duplicate account creation attempts.
     */
    public AccountAlreadyExistsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception raised when insufficient balance conditions occur.
 */
@Getter
public class InsufficientBalanceException extends RuntimeException {

    /** HTTP status that should be returned to the caller. */
    private final HttpStatus status;

    /**
     * Creates an exception when the requested balance is below the required threshold.
     */
    public InsufficientBalanceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

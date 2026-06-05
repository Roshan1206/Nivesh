package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when account not found conditions occur.
 */
@Getter
public class AccountNotFoundException extends RuntimeException {

    /** HTTP status returned for this exception or response. */
    private final HttpStatus status;

    /**
     * Creates an exception when an account cannot be resolved from the repository.
     */
    public AccountNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

}

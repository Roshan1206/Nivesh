package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends RuntimeException {

    /** HTTP status that should be returned to the caller. */
    private final HttpStatus status;

    /**
     * Creates an exception when the requested account product cannot be found.
     */
    public ProductNotFoundException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

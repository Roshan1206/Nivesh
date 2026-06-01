package com.nivesh.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {

    /**
     * Creates an exception when an account cannot be resolved from the repository.
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}

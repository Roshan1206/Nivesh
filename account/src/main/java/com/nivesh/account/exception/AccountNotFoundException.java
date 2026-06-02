package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class AccountNotFoundException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Creates an exception when an account cannot be resolved from the repository.
     */
    public AccountNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }


}

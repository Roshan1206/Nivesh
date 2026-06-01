package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AccountAlreadyExistsException extends RuntimeException {

    private final HttpStatus status;

    public AccountAlreadyExistsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}


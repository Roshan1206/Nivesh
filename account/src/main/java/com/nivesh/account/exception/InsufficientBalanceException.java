package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final HttpStatus status;

    public InsufficientBalanceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
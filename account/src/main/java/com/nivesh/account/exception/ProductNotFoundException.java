package com.nivesh.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends RuntimeException {

    private final HttpStatus status;
    public ProductNotFoundException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
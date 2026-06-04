package com.nivesh.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class SessionExpiredException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public SessionExpiredException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

package com.nivesh.authentication.exception;

import java.time.LocalDateTime;

/**
 *
 */
public class InvalidUserStatusException extends RuntimeException {

    public InvalidUserStatusException(String message) {
        super(message);
    }

    public InvalidUserStatusException(String message, LocalDateTime time) {
        this(message + " Please try after " + time);
    }
}

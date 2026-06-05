package com.nivesh.authentication.exception;

import java.time.LocalDateTime;

/**
 *
 */
public class InvalidUserStatusException extends RuntimeException {

    /**
     * Creates an invalid-user-status exception.
     */
    public InvalidUserStatusException(String message) {
        super(message);
    }

    /**
     * Creates an invalid-user-status exception.
     */
    public InvalidUserStatusException(String message, LocalDateTime time) {
        this(message + " Please try after " + time);
    }
}

package com.nivesh.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception raised when session expired conditions occur.
 */
public class SessionExpiredException extends RuntimeException {

    /** HTTP status returned for this exception or response. */
    @Getter
    private final HttpStatus status;

    /**
     * Creates a session-expired exception with the supplied HTTP status and message.
     */
    public SessionExpiredException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

package com.nivesh.library.exception;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class ServiceUnavailableException extends RuntimeException {

    /**
     * Constructs exception with custom message and logs service unavailability.
     *
     * @param message the error message
     * @param serviceName name of the unavailable service for logging
     */
    public ServiceUnavailableException(String message, String serviceName) {
        super(message);
        log.error(message);
        log.error("{} is down at {}", serviceName, LocalDateTime.now());
    }

    /**
     * Constructs exception with default message.
     *
     * @param serviceName name of the unavailable service
     */
    public ServiceUnavailableException(String serviceName) {
        this("We are experiencing some downtime. Please try again later.", serviceName);
    }
}

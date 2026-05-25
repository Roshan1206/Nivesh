package com.nivesh.library.exception;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, String serviceName) {
        super(message);
        log.error(message);
        log.error("{} is down at {}", serviceName, LocalDateTime.now());
    }

    public ServiceUnavailableException(String serviceName) {
        this("We are experiencing some downtime. Please try again later.", serviceName);
    }
}

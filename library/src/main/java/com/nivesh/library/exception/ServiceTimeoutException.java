package com.nivesh.library.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceTimeoutException extends RuntimeException {

    public ServiceTimeoutException(String message, String serviceName) {
        super(message + serviceName + "is taking too long");
        log.error("{}. {} is taking too long to process.", message, serviceName);
    }
}

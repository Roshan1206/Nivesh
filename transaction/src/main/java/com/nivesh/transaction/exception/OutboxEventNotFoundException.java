package com.nivesh.transaction.exception;

public class OutboxEventNotFoundException extends RuntimeException {
    public OutboxEventNotFoundException(String message) {
        super(message);
    }
}

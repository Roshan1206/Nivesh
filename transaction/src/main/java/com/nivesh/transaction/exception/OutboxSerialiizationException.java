package com.nivesh.transaction.exception;

public class OutboxSerialiizationException extends RuntimeException {
    public OutboxSerialiizationException(String message) {
        super(message);
    }
}

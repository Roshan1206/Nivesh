package com.nivesh.transaction.exception;

/**
 * Exception raised when transaction not found conditions occur.
 */
public class TransactionNotFoundException extends RuntimeException {
    /**
     * Creates a transaction-not-found exception with the provided status and message.
     */
    public TransactionNotFoundException(String message) {
        super(message);
    }
}

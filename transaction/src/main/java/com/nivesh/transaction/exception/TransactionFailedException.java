package com.nivesh.transaction.exception;

/**
 * Exception raised when transaction failed conditions occur.
 */
public class TransactionFailedException extends RuntimeException {

    /**
     * Creates a transaction-failed exception with the provided status and message.
     */
    public TransactionFailedException(String message) {
        super(message);
    }
}

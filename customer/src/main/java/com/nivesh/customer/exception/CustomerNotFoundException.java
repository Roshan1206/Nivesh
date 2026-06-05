package com.nivesh.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when customer not found conditions occur.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    /**
     * Creates a customer-not-found exception with the provided status and message.
     */
    public CustomerNotFoundException() {
        super("Customer not found.");
    }
}

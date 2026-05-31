package com.nivesh.authentication.exception;

/**
 * Thrown when a requested role name does not match any configured role.
 */
public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String message) {
        super(message);
    }
}

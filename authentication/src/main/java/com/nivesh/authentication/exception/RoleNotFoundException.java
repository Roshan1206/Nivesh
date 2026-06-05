package com.nivesh.authentication.exception;

/**
 * Thrown when a requested role name does not match any configured role.
 */
public class RoleNotFoundException extends RuntimeException {

    /**
     * Creates a role-not-found exception with the provided status and message.
     */
    public RoleNotFoundException(String message) {
        super(message);
    }
}

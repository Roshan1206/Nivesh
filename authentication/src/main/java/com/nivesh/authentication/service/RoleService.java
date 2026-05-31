package com.nivesh.authentication.service;

import com.nivesh.authentication.entity.Role;

/**
 * Provides role lookup operations for authentication and authorization workflows.
 */
public interface RoleService {

    /** Returns the configured role for the provided role name. */
    Role getRole(String roleName);
}

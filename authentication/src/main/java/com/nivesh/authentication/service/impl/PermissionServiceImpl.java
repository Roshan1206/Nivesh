package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.repository.PermissionRepository;
import com.nivesh.authentication.service.PermissionService;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Permissions
 *
 * @author Roshan
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    /** Repository used to persist and query permissions. */
    private final PermissionRepository permissionRepository;

    /**
     * Injects the permission repository used to manage permissions.
     */
    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

}

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

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

}

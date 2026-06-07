package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.entity.Role;
import com.nivesh.authentication.exception.RoleNotFoundException;
import com.nivesh.authentication.repository.RoleRepository;
import com.nivesh.authentication.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Roles
 */
@Service
public class RoleServiceImpl implements RoleService {

    /** Repository used to persist and query roles. */
    private final RoleRepository roleRepository;

    /**
     * Injects the role repository used to manage roles.
     */
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    /**
     * Retrieves a Role object based on its role name.
     *
     * @param roleName The name of the role to retrieve.
     * @return The Role object with the specified role name, or null if no such role exists.
     */
    @Transactional(readOnly = true)
    @Override
    public Role getRole(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseThrow(
                () -> new RoleNotFoundException(roleName + " role not found.")
        );
    }
}

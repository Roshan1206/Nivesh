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

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Role getRole(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseThrow(
                () -> new RoleNotFoundException(roleName + " role not found.")
        );
    }
}

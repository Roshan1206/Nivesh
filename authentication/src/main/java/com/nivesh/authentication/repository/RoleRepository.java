package com.nivesh.authentication.repository;

import com.nivesh.authentication.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence access for role definitions and their linked permissions.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /** Finds a role by its unique business name. */
    Optional<Role> findByRoleName(String role);
}

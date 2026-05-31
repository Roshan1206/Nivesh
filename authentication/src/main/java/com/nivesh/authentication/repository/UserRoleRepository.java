package com.nivesh.authentication.repository;

import com.nivesh.authentication.entity.UserRole;
import com.nivesh.authentication.entity.ids.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence access for role assignments attached to users.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}

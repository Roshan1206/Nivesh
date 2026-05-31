package com.nivesh.authentication.repository;

import com.nivesh.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Fetches user with all data needed to build getAuthorities():
     *  - userRoles -> role -> permission (role-based grants)
     *  - permissionOverrides -> permission (per-user GRANT/REVOKE exceptions)
     *  JOIN FETCH is used to avoid LAZY fetch
     */
    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role r
    LEFT JOIN FETCH r.permissions p
    LEFT JOIN FETCH u.permissionOverrides po
    LEFT JOIN FETCH po.permission
    WHERE u.email = :email
    """)
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);


    /**
     * Fetches user by Id with full authority data
     */
    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role r
    LEFT JOIN FETCH r.permissions p
    LEFT JOIN FETCH u.permissionOverrides po
    LEFT JOIN FETCH po.permission
    WHERE u.id = :userId
    """)
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") UUID userId);

}

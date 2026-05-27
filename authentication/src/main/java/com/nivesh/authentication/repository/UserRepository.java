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
     * Select user with roles and permissions with eager fetch
     */
    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role r
    LEFT JOIN FETCH r.permissions
    WHERE u.email = :email
""")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

}

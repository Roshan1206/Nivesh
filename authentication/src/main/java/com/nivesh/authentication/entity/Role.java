package com.nivesh.authentication.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents minimal pre-defined permissions for user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    private UUID id;

    /** Unique role name assigned to users. */
    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    /** Human-readable description for this record. */
    @Column(name = "description", nullable = false)
    private String description;

    /** Indicates whether the role is managed by the system. */
    @Column(name = "is_system_role", nullable = false)
    private boolean isSystemRole;

    /** User-role assignments associated with this role. */
    @OneToMany(
            mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserRole> userRoles = new HashSet<>();

    /** Permissions granted to this role. */
    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

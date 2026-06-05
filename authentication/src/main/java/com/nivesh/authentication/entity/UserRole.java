package com.nivesh.authentication.entity;

import com.nivesh.authentication.entity.ids.UserRoleId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents the user with all of its roles assigned.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles")
public class UserRole {

    /** Unique identifier for this record. */
    @EmbeddedId
    private UserRoleId id;

    /** User associated with this relationship. */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /** Role associated with this relationship. */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    /** User who assigned the role. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    /** Timestamp when the role was assigned. */
    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    /**
     * Creates a user-role assignment and records who assigned it.
     */
    public UserRole(User user, Role role) {
        this.id = new UserRoleId(user.getId(), role.getId());
        this.user = user;
        this.role = role;
        this.assignedBy = user;
        this.assignedAt = Instant.now();
    }

    /**
     * Creates a user-role assignment and records who assigned it.
     */
    public UserRole(User user, Role role, User assignedBy) {
        this.id = new UserRoleId(user.getId(), role.getId());
        this.user = user;
        this.role = role;
        this.assignedBy = assignedBy;
        this.assignedAt = Instant.now();
    }
}

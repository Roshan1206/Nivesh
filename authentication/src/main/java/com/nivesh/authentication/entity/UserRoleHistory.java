package com.nivesh.authentication.entity;

import com.nivesh.authentication.entity.enums.RoleChangeAction;
import com.nivesh.authentication.entity.enums.RoleChangeReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Represent audit trail of every role ASSIGNED or REMOVED for a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_role_history")
public class UserRoleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * User whose rale is changed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Role that was assigned/removed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Whether the role was assigned or removed
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action", nullable = false, columnDefinition = "role_change_action_enum")
    private RoleChangeAction action;

    /**
     * Reason that triggered this role change
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reason", nullable = false, columnDefinition = "role_change_reason_enum")
    private RoleChangeReason reason;

    /**
     * who made this change
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    /**
     * Constructor
     */
    public UserRoleHistory(User user, Role role, RoleChangeAction action,
                           RoleChangeReason reason, User changedBy) {
        this.user = user;
        this.role = role;
        this.action = action;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }
}

package com.nivesh.authentication.entity;

import com.nivesh.authentication.entity.enums.OverrideType;
import com.nivesh.authentication.entity.ids.UserPermissionOverrideId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Represents user permission exceptions on top of role defaults
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_permission_overrides")
public class UserPermissionOverride {

    /**
     * Composite Primary Key
     */
    @EmbeddedId
    private UserPermissionOverrideId id;

    /**
     * The user this override applies to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The permission being granted or revoked for user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    /**
     * GRANT adds the permission, REVOKE blocks it
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "override_type", nullable = false, columnDefinition = "override_type_enum")
    private OverrideType overrideType;

    /** Business reason recorded for this change. */
    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    /** User who granted the permission override. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    /** Timestamp when the permission override was granted. */
    @Column(name = "granted_At", nullable = false)
    private Instant grantedAt;

    /**
     * NULL = never expires. Set a future Instant for temporary overrides.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;
}

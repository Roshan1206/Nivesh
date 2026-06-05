package com.nivesh.authentication.entity;

import com.nivesh.library.entity.enums.Action;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Represents specific permission that a particular user/role needs
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id")
    private UUID id;

    /** Unique code for the permission. */
    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    /** Application resource governed by the permission. */
    @Column(name = "resource", nullable = false)
    private String resource;

    /** Action allowed or audited for the resource. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action_granted", nullable = false, columnDefinition = "action_enum")
    private Action action;

    /** Human-readable description for this record. */
    @Column(name = "description", nullable = false)
    private String description;
}

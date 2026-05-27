package com.nivesh.authentication.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Entity for managing id of UserRole
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserRoleId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;

    private UUID roleId;
}

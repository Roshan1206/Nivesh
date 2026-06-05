package com.nivesh.authentication.entity.ids;

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

    /** Identifier of the user portion of the composite key. */
    private UUID userId;

    /** Identifier of the role portion of the composite key. */
    private UUID roleId;
}

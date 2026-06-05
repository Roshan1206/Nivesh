package com.nivesh.authentication.entity.ids;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite PK for UserPermissionOverride (user_id + permission_id).
 * One override row per user per permission - enforced by PK
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserPermissionOverrideId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifier of the user portion of the composite key. */
    private UUID userId;

    /** Identifier of the permission portion of the composite key. */
    private UUID permissionId;
}

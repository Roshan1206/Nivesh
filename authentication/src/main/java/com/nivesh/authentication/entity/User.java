package com.nivesh.authentication.entity;

import com.nivesh.authentication.entity.enums.OverrideType;
import com.nivesh.library.entity.BaseAudit;
import com.nivesh.library.entity.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a registered user with auditing and roles
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseAudit implements UserDetails {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    /** Registered mobile number for the user. */
    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNumber;

    /** Registered email address for the user. */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Encoded password used for authentication. */
    @Column(name = "password", nullable = false)
    private String password;

    /** Number of consecutive failed login attempts. */
    @Column(name = "failed_attempt")
    private int failedAttempt;

    /** Time until which the user account remains locked. */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    /** Current lifecycle status of the customer account. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "customer_status", columnDefinition = "customer_status_enum", nullable = false)
    private CustomerStatus customerStatus;

    /** Current token version. Incremented in case of blacklisted/revoked */
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    /**
     * Predefined permissions for role
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    /**
     * Permission on top of role based
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserPermissionOverride> permissionOverrides = new HashSet<>();

    /**
     * Builds the authority set for user using roles (base permissions) and
     * extra permission if granted. Removes the expired or revoked roles from authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> revokedCodes = new HashSet<>();
        Instant now = Instant.now();

        for (UserRole ur : userRoles) {
            authorities.add(new SimpleGrantedAuthority(ur.getRole().getRoleName()));
            ur.getRole().getPermissions().forEach(p ->
                    authorities.add(new SimpleGrantedAuthority(p.getPermissionCode())));
        }

        for (UserPermissionOverride override : permissionOverrides) {
            if (override.getExpiresAt() != null && override.getExpiresAt().isBefore(now)) {
                continue;
            }
            String code = override.getPermission().getPermissionCode();
            if (override.getOverrideType() == OverrideType.GRANT) {
                authorities.add(new SimpleGrantedAuthority(code));
            } else {
                revokedCodes.add(code);
            }
        }

        authorities.removeIf(auth -> revokedCodes.contains(auth.getAuthority()));
        return authorities;
    }


    /**
     * Returns the user's password.
     *
     * @return The user's password as a string.
     */
    @Override
    public String getPassword() {
        return password;
    }


    /**
     * Returns the username associated with the user account.
     *
     * @return The username as a String.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     *
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || Instant.now().isAfter(lockedUntil);
    }
}

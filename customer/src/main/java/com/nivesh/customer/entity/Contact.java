package com.nivesh.customer.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * contact_id, customer_id, mobile,
 * email, type(PRIMARY/SECONDARY),
 * verified, updated_at
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contacts")
public class Contact extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contact_id")
    private UUID id;

    /** Customer that owns this record. */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Mobile number for this contact. */
    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNo;

    /** Registered email address for the user. */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Type represented by this record. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contact_type", nullable = false, columnDefinition = "contact_type")
    private ContactType type;

    /** Indicates whether the contact has been verified. */
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    public enum ContactType {
        /** Primary contact for the customer. */
        PRIMARY,

        /** Secondary contact for the customer. */
        SECONDARY
    }
}

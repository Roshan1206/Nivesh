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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contact_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNo;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "contact_type", nullable = false, columnDefinition = "contact_type")
    private ContactType type;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    public enum ContactType {
        PRIMARY,
        SECONDARY
    }
}

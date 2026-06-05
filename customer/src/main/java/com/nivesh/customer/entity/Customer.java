package com.nivesh.customer.entity;

import com.nivesh.customer.entity.enums.Gender;
import com.nivesh.library.entity.BaseAudit;
import com.nivesh.library.entity.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Persistence entity that models customer data in the customer domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customers")
public class Customer extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private UUID id;

    /** Identifier of the user portion of the composite key. */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /** Unique customer number assigned during onboarding. */
    @Column(name = "customer_number", nullable = false, unique = true)
    private String customerNumber;

    /** Customer first name. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Customer middle name, when provided. */
    @Column(name = "middle_name")
    private String middleName;

    /** Customer last name. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Customer date of birth. */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * IFSC Code - Used for identifying customer branch location
     * TODO: Add nullable after making branch service
     */
    @Column(name = "ifsc_code")
    private String ifscCode;

    /** Customer gender value. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, updatable = false, columnDefinition = "gender_enum")
    private Gender gender;

    /** Current KYC verification status for the customer. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kyc_status", nullable = false, columnDefinition = "kyc_status_enum")
    private KycStatus kycStatus;

    /** Addresses registered for the customer. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    /** Contacts registered for the customer. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Contact> contacts = new HashSet<>();

    /** KYC documents submitted by the customer. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<KycDocument> documents = new HashSet<>();

    /** Nominees registered for the customer. */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Nominee> nominees = new HashSet<>();
}

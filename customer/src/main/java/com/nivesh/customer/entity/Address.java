package com.nivesh.customer.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Persistence entity that models address data in the customer domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "addresses")
public class Address extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private UUID id;

    /** Primary street address line. */
    @Column(name = "street_line_1", nullable = false)
    private String streetLine1;

    /** Additional street address line. */
    @Column(name = "street_line_2")
    private String streetLine2;

    /** City for the address. */
    @Column(name = "city", nullable = false)
    private String city;

    /** State for the address. */
    @Column(name = "state", nullable = false)
    private String State;

    /** Postal pin code for the address. */
    @Column(name = "pin_code", nullable = false)
    private String pinCode;

    /** Country for the address. */
    @Column(name = "country", nullable = false)
    private String country;

    /** Type of address captured for the customer. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "address_type", nullable = false, columnDefinition = "address_type_enum")
    private AddressType addressType;

    /** Verification status of the address. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "address_status", nullable = false, columnDefinition = "address_status_enum")
    private AddressStatus addressStatus;

    /** Customer that owns this record. */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    public enum AddressType {
        /** Permanent residence address. */
        PERMANENT,

        /** Current residence address. */
        CURRENT,

        /** Address used for correspondence. */
        CORRESPONDENCE,

        /** Alternate address for the customer. */
        ALTERNATE
    }

    public enum AddressStatus {
        /** Address has not been verified. */
        UNVERIFIED,

        /** Address proof has been submitted. */
        PROOF_SUBMITTED,

        /** Address verification is in progress. */
        VERIFICATION_IN_PROGRESS,

        /** Address has been verified. */
        VERIFIED,

        /** Address verification failed. */
        VERIFICATION_FAILED,

        /** Address must be verified again. */
        REVERIFICATION_REQUIRED
    }
}

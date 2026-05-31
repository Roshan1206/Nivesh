package com.nivesh.customer.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "addresses")
public class Address extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private UUID id;

    @Column(name = "street_line_1", nullable = false)
    private String streetLine1;

    @Column(name = "street_line_2")
    private String streetLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String State;

    @Column(name = "pin_code", nullable = false)
    private String pinCode;

    @Column(name = "country", nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "address_type", nullable = false, columnDefinition = "address_type_enum")
    private AddressType addressType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "address_status", nullable = false, columnDefinition = "address_status_enum")
    private AddressStatus addressStatus;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    public enum AddressType {
        PERMANENT,
        CURRENT,
        CORRESPONDENCE,
        ALTERNATE
    }

    public enum AddressStatus {
        UNVERIFIED,
        PROOF_SUBMITTED,
        VERIFICATION_IN_PROGRESS,
        VERIFIED,
        VERIFICATION_FAILED,
        REVERIFICATION_REQUIRED
    }
}

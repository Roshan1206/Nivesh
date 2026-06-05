package com.nivesh.customer.entity;

import com.nivesh.customer.entity.enums.Gender;
import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Persistence entity that models nominee data in the customer domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nominees")
public class Nominee extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "nominee_id")
    private UUID id;

    /** Customer that owns this record. */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Name captured for this record. */
    @Column(name = "name", nullable = false)
    private String name;

    /** Relationship between the nominee and customer. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "relation", nullable = false, columnDefinition = "relation_enum")
    private Relation relation;

    /** Customer gender value. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, columnDefinition = "gender_enum")
    private Gender gender;

    /** Customer date of birth. */
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /** Percentage share assigned to the nominee. */
    @Column(name = "share_percentage", nullable = false)
    private BigDecimal sharePercentage;

    /** Guardian name for a minor nominee. */
    @Column(name = "guardian_name")
    private String guardianName;

    public enum Relation {

        /** Nominee is the customer's mother. */
        MOTHER,

        /** Nominee is the customer's father. */
        FATHER,

        /** Nominee is the customer's spouse. */
        SPOUSE,

        /** Nominee is the customer's son. */
        SON,

        /** Nominee is the customer's daughter. */
        DAUGHTER
    }
}

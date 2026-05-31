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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nominees")
public class Nominee extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "nominee_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "relation", nullable = false, columnDefinition = "relation_enum")
    private Relation relation;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, columnDefinition = "gender_enum")
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "share_percentage", nullable = false)
    private BigDecimal sharePercentage;

    @Column(name = "guardian_name")
    private String guardianName;

    public enum Relation {

        MOTHER,
        FATHER,
        SPOUSE,
        SON,
        DAUGHTER
    }
}

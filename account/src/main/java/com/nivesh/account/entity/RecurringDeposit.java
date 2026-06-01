package com.nivesh.account.entity;

import com.nivesh.account.entity.enums.Status;
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
 * Details of a Recurring Deposit - customer deposits a fixed amount every month.
 * Missed instalments incur penalty. Maturity amount calculated at opening
 * using compound interest.
 * 
 * @author Roshan 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recurring_deposits")
public class RecurringDeposit extends BaseAccount{

    /** Unique identifier for the recurring deposit record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Which customer account it belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Monthly installment amount for the deposit. Must be in multiple of 100
     */
    @Column(name = "installment", nullable = false)
    private BigDecimal installment;

    /**
     * Tenure for RD in months
     */
    @Column(name = "tenure", nullable = false, updatable = false)
    private Integer tenure;

    /**
     * RD mature date. calculated based on tenure
     */
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    /**
     * Maturity amount.
     */
    @Column(name = "maturity_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal maturityAmount;

    /**
     * Status of RD
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_enum")
    private Status status;

    /**
     * next installment date. Same date in every month
     */
    @Column(name = "next_installment_date", nullable = false)
    private LocalDate nextInstallmentDate;

    /**
     * Missed no of installment. After 3, RD will be broken with penalty
     */
    private int missedCount;
}

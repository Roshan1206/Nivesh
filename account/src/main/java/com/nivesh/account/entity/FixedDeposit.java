package com.nivesh.account.entity;

import com.nivesh.account.entity.enums.PayoutType;
import com.nivesh.account.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Details of a Fixed Deposit linked to a savings/current account. FDs earn higher interest. Premature withdrawal incurs penalty. Auto-renewal renews at
 * prevailing rates on maturity.
 *
 * @author Roshan
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fixed_deposits")
public class FixedDeposit extends BaseAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Which customer account it belongs to.
     */
    @ManyToOne
    @Column(name = "account", nullable = false)
    private Account account;

    /**
     * Principal amount for the deposit. Must be in multiple of 100
     */
    @Column(name = "principal", nullable = false)
    private BigDecimal principal;

    /**
     * Tenure for FD in days
     */
    @Column(name = "tenure", nullable = false, updatable = false)
    private Integer tenure;

    /**
     * FD mature date. calculated based on tenure
     */
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    /**
     * Last interest payout date. When the last pay happened.
     */
    @Column(name = "last_interest_payout", insertable = false)
    private LocalDate lastInterestPayoutDate;

    /**
     * Next interest payout date. Only applicable if interest payout is not maturity
     */
    @Column(name = "next_interest_payout", nullable = false)
    private LocalDate nextInterestPayoutDate;

    /**
     * Maturity amount.
     */
    @Column(name = "maturity_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal maturityAmount;

    /**
     * When the interest should be paid.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "product_type", nullable = false, updatable = false, columnDefinition = "payout_type_enum")
    private PayoutType payoutType;

    /**
     * If the FD is marked for auto-renewable
     */
    @Column(name = "auto_renewable", nullable = false)
    private boolean autoRenewal;

    /**
     * Status of FD
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_enum")
    private Status status;

    /**
     * FD closed date
     */
    @Column(name = "closed_at")
    private LocalDate closedAt;

}

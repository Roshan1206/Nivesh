package com.nivesh.account.entity;

import com.nivesh.account.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The master record for a bank account. Each account has an 11-digit account number, belongs to one customer, and maintains
 * a DECIMAL(20,4) balance for precision. Status drives what operations are permitted
 *
 * @author Roshan
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends BaseAccount{

    /**
     * UUID is used for account id. Unique and random
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Account belongs to which customer. Must be present in customer service
     */
    @Column(name = "customer_number", nullable = false, updatable = false)
    private String customerNumber;

    /**
     * Determines the type of account
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;



    /**
     * Total balance - including hold amount
     */
    @Column(name = "balance", nullable = false, precision = 20, scale = 2)
    private BigDecimal balance;

    /**
     * Current available balance
     */
    @Column(name = "available_balance", nullable = false, precision = 20, scale = 2)
    private BigDecimal availableBalance;

    /**
     * Account status for transactions
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_enum")
    private Status status;

    /**
     * Account last updated date. Cannot be insertable
     */
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}

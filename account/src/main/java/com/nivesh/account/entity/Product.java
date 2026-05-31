package com.nivesh.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration table defining rules for each account type. Drives minimum balance, interest rates, withdrawal limits, and feature flags
 *
 * @author Roshan
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Product code
     */
    @Column(name = "product_code", columnDefinition = "CHAR(3)", nullable = false, unique = true)
    private String productCode;

    /**
     * Product name
     */
    @Column(name = "product_name", nullable = false, unique = true)
    private String productName;

    /**
     * Minimum balance for the product
     */
    @Column(name = "min_balance", nullable = false, precision = 20, scale = 2)
    private BigDecimal minBalance;

    /**
     * Updated interest rate for the product
     */
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * Updated max withdrawal limit for the product
     */
    @Column(name = "max_withdrawal_limit", precision = 20, scale = 2, nullable = false)
    private BigDecimal maxWithdrawalLimit;

    /**
     * Features of the product
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    private Map<String, Object> features;

    /**
     * Whether this product is active or discontinued.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts;
}

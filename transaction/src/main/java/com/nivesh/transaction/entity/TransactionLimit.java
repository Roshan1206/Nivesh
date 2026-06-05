package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.TransactionChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity that models transaction limit data in the transaction domain.
 */
@Entity
@Table(
    name = "transaction_limits",
    // Composite unique: one limit row per account per channel
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_txn_limit_account_channel",
            columnNames = {"account_id", "channel"}
        )
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TransactionLimit {

    /** Unique identifier for the transaction limit record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "limit_id", updatable = false, nullable = false)
    private UUID limitId;

    /** Identifier of the account associated with this record. */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** Transaction channel governed by this limit. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "channel", nullable = false, length = 20, columnDefinition = "transaction_channel")
    private TransactionChannel channel;

    /** Maximum amount allowed for the day. */
    @Column(name = "daily_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal dailyLimit;

    /** Maximum amount allowed for a single transaction. */
    @Column(name = "per_txn_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal perTxnLimit;

    /** Maximum amount allowed for the month. */
    @Column(name = "monthly_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal monthlyLimit;

    /** Amount already used against the daily limit. */
    @Column(name = "daily_used", nullable = false, precision = 20, scale = 4)
    @Builder.Default
    private BigDecimal dailyUsed = BigDecimal.ZERO;

    /** Amount already used against the monthly limit. */
    @Column(name = "monthly_used", nullable = false, precision = 20, scale = 4)
    @Builder.Default
    private BigDecimal monthlyUsed = BigDecimal.ZERO;

    /** Date on which usage counters were last reset. */
    @Column(name = "last_reset", nullable = false)
    private LocalDate lastReset;

    /** Timestamp when this record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

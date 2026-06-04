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
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "limit_id", updatable = false, nullable = false)
    private UUID limitId;
 
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
 
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "channel", nullable = false, length = 20, columnDefinition = "transaction_channel")
    private TransactionChannel channel;
 
    @Column(name = "daily_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal dailyLimit;
 
    @Column(name = "per_txn_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal perTxnLimit;
 
    @Column(name = "monthly_limit", nullable = false, precision = 20, scale = 4)
    private BigDecimal monthlyLimit;
 
    @Column(name = "daily_used", nullable = false, precision = 20, scale = 4)
    @Builder.Default
    private BigDecimal dailyUsed = BigDecimal.ZERO;
 
    @Column(name = "monthly_used", nullable = false, precision = 20, scale = 4)
    @Builder.Default
    private BigDecimal monthlyUsed = BigDecimal.ZERO;
 
    @Column(name = "last_reset", nullable = false)
    private LocalDate lastReset;
 
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
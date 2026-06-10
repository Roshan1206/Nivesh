package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.TransactionChannel;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.entity.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity that models transaction data in the transaction domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
@Entity
public class Transaction {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "txn_id", updatable = false)
    private UUID id;

    /** External reference number for the transaction. */
    @Column(name = "reference_number", nullable = false, updatable = false, unique = true, length = 16)
    private String referenceNumber;

    /** Type of transaction being processed. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", updatable = false, nullable = false, columnDefinition = "transaction_type")
    private TransactionType transactionType;

    /** Configuration that controls this transaction type. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code", referencedColumnName = "type_code", nullable = false, updatable = false)
    private TransactionTypeConfig typeConfig;

    /** Source account identifier for debit-side processing. */
    @Column(name = "source_account_id", updatable = false)
    private UUID sourceAccountId;

    /** Destination account identifier for credit-side processing. */
    @Column(name = "destination_account_id", updatable = false)
    private UUID destinationAccountId;

    /** External party reference used for off-book payments. */
    @Column(name = "external_party_ref", updatable = false)
    private String externalPartyRef;

    /** Monetary amount associated with this record. */
    @Column(name = "amount", nullable = false, updatable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    /** Current status of this record. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "transaction_status")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.INITIATED;

    /** Channel through which the transaction was initiated. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "channel", nullable = false, updatable = false, columnDefinition = "transaction_channel")
    private TransactionChannel transactionChannel;

    /** Client-provided key used to prevent duplicate account operations. */
    @Column(name = "idempotency_key", unique = true, nullable = false, updatable = false)
    private String idempotencyKey;

    /** Human-readable description for this record. */
    @Column(name = "description")
    private String description;

    /** Timestamp when this record was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the transaction was settled. */
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    /** Transaction that reverses this transaction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_txn_id",
                foreignKey = @ForeignKey(name = "fk_txn_reversak"))
    private Transaction reversalTxnId;

    /** Identifier of the user or process that initiated the transaction. */
    @Column(name = "initiated_by", updatable = false)
    private UUID initiatedBy;

    @Builder.Default
    @Column(name = "credit_retry_count", nullable = false)
    private int creditRetryCount = 0;

    @Builder.Default
    @Column(name = "compensate_retry_count", nullable = false)
    private int compensateRetryCount = 0;
}

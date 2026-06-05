package com.nivesh.account.entity;

import com.nivesh.account.entity.enums.OperationType;
import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity that models idempotency record data in the account domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "idempotency_records")
@Entity
public class IdempotencyRecord extends BaseAudit {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Client-provided key used to prevent duplicate account operations. */
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    /** Type represented by this record. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, updatable = false, columnDefinition = "operation_type_enum")
    private OperationType type;

    /** Identifier of the account associated with this record. */
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    /** Monetary amount associated with this record. */
    @Column(name = "amount", nullable = false, scale = 20, precision = 4, updatable = false)
    private BigDecimal amount;

    /** Account balance captured after the operation is applied. */
    @Column(name = "running_balance", nullable = false, scale = 20, precision = 4, updatable = false)
    private BigDecimal runningBalance;

    /** HTTP response status code cached for idempotent replay. */
    @Column(name = "response_status_code", nullable = false)
    private int responseStatusCode;

    /** Timestamp after which this idempotency record can expire. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

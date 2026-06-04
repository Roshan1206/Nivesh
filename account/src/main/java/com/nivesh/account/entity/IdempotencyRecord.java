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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "idempotency_records")
@Entity
public class IdempotencyRecord extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, updatable = false, columnDefinition = "operation_type_enum")
    private OperationType type;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "amount", nullable = false, scale = 20, precision = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "running_balance", nullable = false, scale = 20, precision = 4, updatable = false)
    private BigDecimal runningBalance;

    @Column(name = "response_status_code", nullable = false)
    private int responseStatusCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}

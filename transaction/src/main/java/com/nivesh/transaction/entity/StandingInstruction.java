package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.StandingInstructionFrequency;
import com.nivesh.transaction.entity.enums.StandingInstructionStatus;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity that models standing instruction data in the transaction domain.
 */
@Entity
@Table(name = "standing_instructions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class StandingInstruction {

    /** Unique identifier for the standing instruction. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "si_id", updatable = false, nullable = false)
    private UUID siId;

    /** Identifier of the account associated with this record. */
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    /** Identifier of the beneficiary for the instruction. */
    @Column(name = "beneficiary_id", nullable = false, updatable = false)
    private UUID beneficiaryId;

    /** Monetary amount associated with this record. */
    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    /** Execution frequency for the standing instruction. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "frequency", nullable = false, length = 20, updatable = false, columnDefinition = "si_frequency")
    private StandingInstructionFrequency frequency;

    /** Configured day on which the instruction executes. */
    @Column(name = "execution_day")
    private Integer executionDay;

    /** Next date on which the instruction should run. */
    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    /** Start date for the standing instruction. */
    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate;

    /** End date for the standing instruction. */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Maximum number of times the instruction may execute. */
    @Column(name = "max_executions")
    private Integer maxExecutions;

    /** Number of times the instruction has executed. */
    @Column(name = "executed_count", nullable = false)
    @Builder.Default
    private int executedCount = 0;

    /** Current status of this record. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "si_status")
    @Builder.Default
    private StandingInstructionStatus status = StandingInstructionStatus.ACTIVE;

    /** Most recent failure reason for the instruction. */
    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    /** Identifier of the user or service that created the record. */
    // UUID of customer or RM who created this SI.
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    /** Timestamp when this record was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

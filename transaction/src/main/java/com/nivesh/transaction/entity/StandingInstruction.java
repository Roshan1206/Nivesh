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

@Entity
@Table(name = "standing_instructions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class StandingInstruction {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "si_id", updatable = false, nullable = false)
    private UUID siId;
 
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;
 
    @Column(name = "beneficiary_id", nullable = false, updatable = false)
    private UUID beneficiaryId;
 
    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;
 
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "frequency", nullable = false, length = 20, updatable = false, columnDefinition = "si_frequency")
    private StandingInstructionFrequency frequency;
 
    @Column(name = "execution_day")
    private Integer executionDay;
 
    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;
 
    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate;
 
    @Column(name = "end_date")
    private LocalDate endDate;
 
    @Column(name = "max_executions")
    private Integer maxExecutions;
 
    @Column(name = "executed_count", nullable = false)
    @Builder.Default
    private int executedCount = 0;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "si_status")
    @Builder.Default
    private StandingInstructionStatus status = StandingInstructionStatus.ACTIVE;
 
    @Column(name = "failure_reason", length = 100)
    private String failureReason;
 
    // UUID of customer or RM who created this SI.
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
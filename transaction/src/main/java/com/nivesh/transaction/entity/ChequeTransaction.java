package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.ChequeStatus;
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
import jakarta.persistence.OneToOne;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cheque_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChequeTransaction {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cheque_txn_id", updatable = false, nullable = false)
    private UUID chequeTxnId;
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_id", nullable = false, unique = true,
                updatable = false,
                foreignKey = @ForeignKey(name = "fk_cheque_txn_transaction"))
    private Transaction transaction;
 
    @Column(name = "cheque_number", nullable = false, length = 6, updatable = false)
    private String chequeNumber;
 
    @Column(name = "drawee_bank_ifsc", nullable = false, length = 11, updatable = false)
    private String draweeBankIfsc;
 
    @Column(name = "micr_code", nullable = false, length = 9, updatable = false)
    private String micrCode;
 
    @Column(name = "presented_date", nullable = false, updatable = false)
    private LocalDate presentedDate;
 
    @Column(name = "clearing_date", nullable = false)
    private LocalDate clearingDate;
 
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "cheque_status")
    @Builder.Default
    private ChequeStatus status = ChequeStatus.PRESENTED;
 
    @Column(name = "bounce_reason", length = 50)
    private String bounceReason;
 
    @Column(name = "cts_reference", length = 30)
    private String ctsReference;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
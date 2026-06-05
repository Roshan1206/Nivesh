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

/**
 * Persistence entity that models cheque transaction data in the transaction domain.
 */
@Entity
@Table(name = "cheque_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ChequeTransaction {

    /** Unique identifier for the cheque transaction record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cheque_txn_id", updatable = false, nullable = false)
    private UUID chequeTxnId;

    /** Transaction associated with this detail record. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_id", nullable = false, unique = true,
                updatable = false,
                foreignKey = @ForeignKey(name = "fk_cheque_txn_transaction"))
    private Transaction transaction;

    /** Cheque number printed on the instrument. */
    @Column(name = "cheque_number", nullable = false, length = 6, updatable = false)
    private String chequeNumber;

    /** IFSC code of the drawee bank. */
    @Column(name = "drawee_bank_ifsc", nullable = false, length = 11, updatable = false)
    private String draweeBankIfsc;

    /** MICR code printed on the cheque. */
    @Column(name = "micr_code", nullable = false, length = 9, updatable = false)
    private String micrCode;

    /** Date on which the cheque was presented. */
    @Column(name = "presented_date", nullable = false, updatable = false)
    private LocalDate presentedDate;

    /** Date on which cheque clearing completed. */
    @Column(name = "clearing_date", nullable = false)
    private LocalDate clearingDate;

    /** Current status of this record. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "cheque_status")
    @Builder.Default
    private ChequeStatus status = ChequeStatus.PRESENTED;

    /** Reason recorded when the cheque is bounced. */
    @Column(name = "bounce_reason", length = 50)
    private String bounceReason;

    /** Cheque truncation system reference for clearing. */
    @Column(name = "cts_reference", length = 30)
    private String ctsReference;

    /** Timestamp when this record was created. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

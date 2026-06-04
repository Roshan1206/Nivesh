package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.AtmRejectReason;
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
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "atm_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AtmTransaction {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "atm_txn_id", updatable = false, nullable = false)
    private UUID atmTxnId;
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_id", nullable = false, unique = true,
                updatable = false,
                foreignKey = @ForeignKey(name = "fk_atm_txn_transaction"))
    private Transaction transaction;
 
    // Which physical ATM terminal processed this.
    // UUID from Branch & ATM Service (port 8093).
    @Column(name = "atm_id", nullable = false, updatable = false)
    private UUID atmId;
 
    // Which debit card was used.
    // UUID from Cards Service (port 8087).
    @Column(name = "card_id", nullable = false, updatable = false)
    private UUID cardId;
 
    // False = customer did not enter correct PIN.
    // Triggers automatic fraud review flag.
    // After 3 false PINs in a session → card blocked.
    @Column(name = "pin_verified", nullable = false, updatable = false)
    private boolean pinVerified;
 
    // What the customer requested.
    // May differ from dispensed_amount if ATM ran low on cash.
    @Column(name = "requested_amount", nullable = false,
            precision = 20, scale = 4, updatable = false)
    private BigDecimal requestedAmount;
 
    // Actual cash physically dispensed.
    // Stored for dispute resolution: "I asked for ₹5000 but got ₹4800"
    @Column(name = "dispensed_amount", nullable = false,
            precision = 20, scale = 4)
    private BigDecimal dispensedAmount;
 
    // Null on success. Reason code if ATM declined.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reject_reason", length = 30, columnDefinition = "atm_reject_reason")
    private AtmRejectReason rejectReason;
 
    // ISO 8583 sequence number from the ATM switch.
    // Used for inter-system reconciliation with the ATM vendor.
    @Column(name = "atm_sequence_number", length = 20, updatable = false)
    private String atmSequenceNumber;
 
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
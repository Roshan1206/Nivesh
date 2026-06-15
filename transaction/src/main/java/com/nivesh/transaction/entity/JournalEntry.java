package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.DrCr;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity that models journal entry data in the transaction domain.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "journal_entries")
@Entity
public class JournalEntry {

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id", updatable = false, nullable = false)
    private UUID id;

    /** Transaction associated with this detail record. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_id", nullable = false, updatable = false)
    private Transaction transaction;

    /** Identifier of the account associated with this record. */
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    /** General ledger account used for this journal entry. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false, updatable = false)
    private GLAccount glAccountId;

    /** Debit or credit indicator for the journal entry. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "dr_cr", nullable = false, updatable = false, columnDefinition = "dr_cr")
    private DrCr drCr;

    /** Monetary amount associated with this record. */
    @Column(name = "amount", nullable = false, updatable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    /** Account balance captured after the operation is applied. */
    @Column(name = "running_balance", nullable = false, updatable = false, precision = 20, scale = 2)
    private BigDecimal runningBalance;

    /** Narration recorded for the journal entry. */
    @Column(name = "narration", updatable = false)
    private String narration;

    /** Timestamp when the journal entry was posted. */
    @CreationTimestamp
    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;
}

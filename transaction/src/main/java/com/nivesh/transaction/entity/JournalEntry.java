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
@Table(name = "journal_entries")
@Entity
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "txn_id", nullable = false, updatable = false)
    private Transaction transaction;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false, updatable = false)
    private GLAccount glAccountId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "dr_cr", nullable = false, updatable = false, columnDefinition = "dr_cr")
    private DrCr drCr;

    @Column(name = "amount", nullable = false, updatable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    @Column(name = "running_balance", nullable = false, updatable = false, precision = 20, scale = 2)
    private BigDecimal runningBalance;

    @Column(name = "narration", updatable = false)
    private String narration;

    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;
}

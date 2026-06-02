package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.TransactionFlow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "entry_id")
    private UUID id;

    private Transaction transaction;

    private UUID accountId;

    private UUID glAccountId;

    private TransactionFlow drCr;

    private BigDecimal amount;

    private BigDecimal runningBalance;

    private String narration;

    private Instant postedAt;
}

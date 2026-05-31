package com.nivesh.account.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base account for different accounts. Extend {@link BaseAudit} for auditing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseAccount extends BaseAudit {

    /**
     * Account number must be of 11 digit and unique
     */
    @Column(name = "account_number", nullable = false, unique = true, updatable = false)
    private String accountNumber;

    /**
     * Interest rate for the account
     */
    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    /**
     * Nominee ID.
     */
    @Column(name = "nomination_id")
    private UUID nominationId;
}

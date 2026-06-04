package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.SettlementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction_type_configs")
@Entity
public class TransactionTypeConfig {

    @Id
    @Column(name = "type_code", nullable = false)
    private String typeCode;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GLAccount glAccount;

    @Column(name = "max_limit_daily", nullable = false, precision = 20, scale = 4)
    private BigDecimal maxDailyLimit;

    @Column(name = "charge_applicable", nullable = false)
    private boolean chargeApplicable;

    @Column(name = "reversal_allowed", nullable = false)
    private boolean reversalAllowed;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "settlement_type", nullable = false, columnDefinition = "settlement_type")
    private SettlementType settlementType;

    @Column(name = "requires_beneficiary", nullable = false)
    private boolean requiresBeneficiary;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}

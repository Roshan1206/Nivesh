package com.nivesh.transaction.entity;

import com.nivesh.library.entity.enums.TransactionChannel;
import com.nivesh.library.entity.enums.TransactionStatus;
import com.nivesh.library.entity.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "txn_id")
    private UUID id;

    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type", nullable = false, columnDefinition = "transaction_type_enum")
    private TransactionType type;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "destination_account_id")
    private String destinationAccountId;

    private BigDecimal amount;

    private TransactionStatus status;

    private TransactionChannel channel;

    private UUID idempotencyKey;

    private String description;

    private String createdAt;

    private String settledAt;

    private String reversalTxnId;

    private UUID initiatedBy;
}

package com.nivesh.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingTransaction {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String typeCode;
    private String description;
}

package com.nivesh.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequestedEvent {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;
    private String idempotencyKey;
    private String referenceNumber;
    private String transactionType;
}

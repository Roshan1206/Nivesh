package com.nivesh.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestedEvent {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;
    private String idempotencyKey;
    private String referenceNumber;
    private String transactionType;
}

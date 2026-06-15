package com.nivesh.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DebitRequestedEvent {

    private UUID accountId;
    private BigDecimal amount;
    private String idempotencyKey;
    private String referenceNumber;
    private String transactionType;
}

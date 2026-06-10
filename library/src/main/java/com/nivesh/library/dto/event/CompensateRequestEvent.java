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
public class CompensateRequestEvent {

    private String referenceNumber;
    private String idempotencyKey;
    private UUID sourceAccountId;
    private BigDecimal amount;
}

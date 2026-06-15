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
public class DebitResultEvent {

    private String referenceNumber;
    private UUID sourceAccountId;
    private BigDecimal amount;
    private BigDecimal runningBalance;
    private boolean success;
    private String failureReason;
}

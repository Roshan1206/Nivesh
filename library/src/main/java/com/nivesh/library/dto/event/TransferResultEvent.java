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
public class TransferResultEvent {

    private TransferRequestedEvent transferRequest;
    private BigDecimal postDebitBalance;
    private BigDecimal postCreditBalance;
    private boolean success;
    private String failureReason;
}

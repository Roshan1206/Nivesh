package com.nivesh.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload used by the library API for amount transaction request operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmountTransactionRequest {

    /** Monetary amount for the request or response. */
    private BigDecimal amount;
}

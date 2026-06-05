package com.nivesh.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload used by the library API for transaction request operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    /** Source account number for the transaction. */
    private String sourceAccountNumber;

    /** Destination account number for the transaction. */
    private String destinationAccountNumber;

    /** Monetary amount for the request or response. */
    private BigDecimal amount;

    /** Human-readable description for the request or record. */
    private String description;
}

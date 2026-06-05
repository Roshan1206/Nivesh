package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response payload returned by the library API for account validation response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResponse {

    /** Resolved source account identifier. */
    private UUID sourceAccountId;

    /** Resolved destination account identifier. */
    private UUID destinationAccountId;

    /** Monetary amount for the request or response. */
    private BigDecimal amount;

    /** Indicates whether the account has sufficient balance. */
    private boolean isBalanceSufficient;

}

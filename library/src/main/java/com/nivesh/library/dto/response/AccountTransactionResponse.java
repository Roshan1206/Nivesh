package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/**
 * Response payload returned by the library API for account transaction response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransactionResponse {

    /** HTTP status returned for this exception or response. */
    private int status;

    /** Account balance after the operation is applied. */
    private BigDecimal runningBalance;
}

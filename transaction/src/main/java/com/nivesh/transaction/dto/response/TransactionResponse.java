package com.nivesh.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Response payload returned by the transaction API for transaction response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    /** Reference id value used by this component. */
    private String referenceId;

    /** Status returned by the operation. */
    private HttpStatus status;

    /** Message returned to the client. */
    private String message;
}

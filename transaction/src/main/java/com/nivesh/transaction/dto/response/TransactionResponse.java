package com.nivesh.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String referenceId;
    private HttpStatus status;
    private String message;
}

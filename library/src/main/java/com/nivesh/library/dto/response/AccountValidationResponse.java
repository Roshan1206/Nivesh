package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResponse {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;
    private boolean isBalanceSufficient;


}

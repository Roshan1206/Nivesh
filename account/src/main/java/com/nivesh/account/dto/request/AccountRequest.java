package com.nivesh.account.dto.request;

import com.nivesh.library.annotation.ValidateCustomerNumber;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO class for creating Account.
 *
 * @author Roshan
 */
@Data
public class AccountRequest {

    @ValidateCustomerNumber
    private String customerNumber;

    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Pattern(regexp = "^\\d{3}$", message = "Must be of length 3")
    private String productCode;
}
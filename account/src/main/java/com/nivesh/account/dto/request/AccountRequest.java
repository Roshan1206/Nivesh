package com.nivesh.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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

    @Pattern(regexp = "^\\d{8}$", message = "Customer Number can only be of 8 digits")
    private String customerNumber;

    @Pattern(regexp = "^[A-Z0-9]{11}$", message = "Invalid IFSC code")
    private String ifscCode;

    @DecimalMin(value = "100.00", message = "Must be greater than and multiple of 100")
    private BigDecimal openingBalance;

    @Pattern(regexp = "^\\d{3}$", message = "Must be of length 3")
    private String productCode;
}

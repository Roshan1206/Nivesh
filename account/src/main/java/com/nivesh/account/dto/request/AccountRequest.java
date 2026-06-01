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

    /** Customer number owning the new account. */
    @ValidateCustomerNumber
    private String customerNumber;

    /** Opening balance to seed the account; defaults to zero when omitted. */
    private BigDecimal openingBalance = BigDecimal.ZERO;

    /** Three-digit product code that determines account rules and number generation. */
    @Pattern(regexp = "^\\d{3}$", message = "Must be of length 3")
    private String productCode;
}

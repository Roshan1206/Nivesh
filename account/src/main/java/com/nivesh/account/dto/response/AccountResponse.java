package com.nivesh.account.dto.response;

import com.nivesh.account.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO class for successful account creation.
 *
 * @author Roshan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;
    private String customerNumber;
    private Status status;
    private String productCode;
    private String productName;
    private BigDecimal availableBalance;
}
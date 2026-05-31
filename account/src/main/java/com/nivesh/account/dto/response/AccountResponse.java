package com.nivesh.account.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO class for successful account creation.
 *
 * @author Roshan
 */
@Data
public class AccountResponse {

    private String name;
    private String accountNumber;
    private String customerNumber;
    private String address;
    private String pinCode;
    private String ifscCode;
    private String branchCode;
    private String branchAddress;
    private String productCode;
    private String productName;
    private BigDecimal availableBalance;
}

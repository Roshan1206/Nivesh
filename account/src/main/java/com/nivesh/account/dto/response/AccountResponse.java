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

    /** Generated account number exposed to customers. */
    private String accountNumber;

    /** Customer number associated with the account. */
    private String customerNumber;

    /** Current lifecycle status of the account. */
    private Status status;

    /** Product code used to open the account. */
    private String productCode;

    /** Human-readable product name. */
    private String productName;

    /** Balance currently available for withdrawal or transfer. */
    private BigDecimal availableBalance;
}

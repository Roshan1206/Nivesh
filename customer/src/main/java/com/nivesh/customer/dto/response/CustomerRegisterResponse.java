package com.nivesh.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned by the customer API for customer register response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterResponse {
    /** Customer number value used by this component. */
    private String customerNumber;

    /** Customer name value used by this component. */
    private String customerName;

    /** Email address supplied by the client. */
    private String email;

    /** Mobile number returned to or supplied by the client. */
    private String mobileNumber;
}

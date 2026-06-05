package com.nivesh.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned by the customer API for KYC initiate response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiateResponse {

    /** Message returned to the client. */
    private String message;

    /** Request id value used by this component. */
    private String requestId;

    /**
     * Creates a KYC initiation response with the OTP request identifier.
     */
    public KycInitiateResponse(String requestId) {
        this.requestId = requestId;
        this.message = "Otp has been sent to mobile linked to Aadhar Card";
    }
}

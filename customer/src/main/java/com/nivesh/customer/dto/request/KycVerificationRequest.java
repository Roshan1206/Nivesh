package com.nivesh.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used by the customer API for KYC verification request operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationRequest {
    /** Request id value used by this component. */
    private String requestId;

    /** One-time password submitted by the client. */
    private String otp;
}

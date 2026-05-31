package com.nivesh.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationRequest {
    private String requestId;
    private String otp;
}

package com.nivesh.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiateResponse {

    private String message;
    private String requestId;

    public KycInitiateResponse(String requestId) {
        this.requestId = requestId;
        this.message = "Otp has been sent to mobile linked to Aadhar Card";
    }
}

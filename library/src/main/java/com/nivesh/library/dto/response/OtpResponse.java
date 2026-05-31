package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpResponse {

    String message;
    String requestId;

    public OtpResponse(String requestId){
        this.message = "Otp has been sent to given email. Please verify to complete registration.";
        this.requestId = requestId;
    }
}

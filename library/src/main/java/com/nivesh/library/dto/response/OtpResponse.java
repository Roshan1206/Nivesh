package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class OtpResponse {

    /** Response message about OTP delivery status */
    private final String message;
    /** Unique identifier to track and verify the OTP */
    private final String requestId;

    public OtpResponse(String requestId){
        this.message = "Otp has been sent to given email. Please verify to complete registration.";
        this.requestId = requestId;
    }
}

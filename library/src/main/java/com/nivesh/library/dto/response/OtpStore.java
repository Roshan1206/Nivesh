package com.nivesh.library.dto.response;

/**
 * DTO class for Otp with its request id;
 */
public record OtpStore(String plainOtp, String otpRequestId) {
}

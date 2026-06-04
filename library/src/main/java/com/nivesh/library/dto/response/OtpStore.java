package com.nivesh.library.dto.response;

import java.util.UUID;

/**
 * DTO containing the generated OTP and its tracking request ID.
 * Returned to the client after successful OTP generation.
 * @param plainOtp Plain-text OTP to be sent to the user
 * @param otpRequestId Unique identifier to track and link the OTP for verification
 */
public record OtpStore(String plainOtp, UUID otpRequestId) {
}

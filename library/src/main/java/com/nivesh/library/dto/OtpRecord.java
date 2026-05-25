package com.nivesh.library.dto;

import java.time.LocalDateTime;

/**
 * Record class for manging OTP.
 *
 * @author Roshan
 */
public record OtpRecord(String otp, LocalDateTime expiresAt, String customerNumber) {

    /**
     * Validates if OTP is still valid or not
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

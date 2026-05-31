package com.nivesh.library.dto.request;

import com.nivesh.library.entity.enums.OtpPurpose;
import lombok.Getter;

import java.time.Instant;

/**
 * Represents an OTP entry stored in cache with expiration and attempt tracking.
 */
public class OtpEntry {

    /** Bcrypt-encoded OTP hash */
    @Getter
    private final String otpHash;

    /** Purpose for which OTP was generated */
    private final OtpPurpose purpose;

    /** Timestamp when OTP was created */
    private final Instant createdAt;

    /** Timestamp when OTP expires */
    private final Instant expiresAt;

    /** Current validation attempt count */
    @Getter
    private int attemptCount;

    /** Maximum allowed validation attempts */
    private final int maxAttempt;

    /** Unique identifier for the OTP request */
    @Getter
    private final String requestId;

    public OtpEntry(String otpHash, String requestId, OtpPurpose purpose, int ttlSecond, int maxAttempt) {
        this.otpHash = otpHash;
        this.purpose = purpose;
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(ttlSecond);
        this.attemptCount = 0;
        this.maxAttempt = maxAttempt;
        this.requestId = requestId;
    }

    /**
     * Increments the failed validation attempt count.
     */
    public void incrementCount() {
        this.attemptCount++;
    }

    /**
     * Checks if the OTP has expired.
     *
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if maximum validation attempts have been exceeded.
     *
     * @return true if attempt count meets or exceeds max limit
     */
    public boolean isMaxAttemptReached() {
        return attemptCount >= maxAttempt;
    }

    /**
     * Calculates remaining validation attempts.
     *
     * @return number of attempts still available
     */
    public int getRemainingAttempts() {
        return maxAttempt - attemptCount;
    }
}

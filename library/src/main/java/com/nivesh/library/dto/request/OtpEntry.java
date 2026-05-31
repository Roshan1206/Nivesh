package com.nivesh.library.dto.request;

import com.nivesh.library.entity.enums.OtpPurpose;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class OtpEntry {

    @Getter
    private final String otpHash;

    private final OtpPurpose purpose;

    private final Instant createdAt;

    private final Instant expiresAt;

    @Getter
    private int attemptCount;

    private final int maxAttempt;

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

    public void incrementCount() {
        this.attemptCount++;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isMaxAttemptReached() {
        return attemptCount >= maxAttempt;
    }

    public int getRemainingAttempts() {
        return maxAttempt - attemptCount;
    }
}

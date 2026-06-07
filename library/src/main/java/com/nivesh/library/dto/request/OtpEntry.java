package com.nivesh.library.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivesh.library.entity.enums.OtpPurpose;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an OTP entry stored in cache with expiration and attempt tracking.
 */
@Getter
public class OtpEntry {

    /** Bcrypt-encoded OTP hash */
    private final String otp;

    /** Current validation attempt count */
    private int attemptCount;

    /** Maximum allowed validation attempts */
    private final int maxAttempt = 3;


    /**
     * Creates an OTP cache entry with the generated code and attempt metadata.
     */
    public OtpEntry(String otp) {
        this.otp = otp;
        this.attemptCount = 0;
    }

    @JsonCreator
    public OtpEntry(@JsonProperty("otp") String otp, @JsonProperty("attemptCount") int attemptCount) {
        this.otp = otp;
        this.attemptCount = attemptCount;
    }

    /**
     * Increments the failed validation attempt count.
     */
    @JsonIgnore
    public void incrementCount() {
        this.attemptCount++;
    }


    /**
     * Checks if maximum validation attempts have been exceeded.
     *
     * @return true if attempt count meets or exceeds max limit
     */
    @JsonIgnore
    public boolean isMaxAttemptReached() {
        return attemptCount >= maxAttempt;
    }

    /**
     * Calculates remaining validation attempts.
     *
     * @return number of attempts still available
     */
    @JsonIgnore
    public int getRemainingAttempts() {
        return maxAttempt - attemptCount;
    }
}

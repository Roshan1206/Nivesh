package com.nivesh.library.entity.enums;

/**
 * Represents the KYC (Know Your Customer) verification status of a customer.
 * Tracks the progress of identity and document verification.
 *
 * @author Roshan
 */
public enum KycStatus {

    /**
     * Initial state, awaiting KYC submission
     */
    PENDING,

    /**
     * KYC documents are being actively reviewed
     */
    IN_PROGRESS,

    /**
     * Customer has passed all KYC verification checks
     */
    VERIFIED,

    /**
     * KYC submission was rejected, resubmission required
     */
    REJECTED,

    /**
     * Previously verified KYC has expired, renewal needed
     */
    EXPIRED
}

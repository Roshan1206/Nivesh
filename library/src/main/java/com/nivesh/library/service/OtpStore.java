package com.nivesh.library.service;

/**
 * Interface for Otp services
 */
public interface OtpStore {

    void save(String identifier, String otp, String customerNumber);

    String verify(String identifier, String otp);
}

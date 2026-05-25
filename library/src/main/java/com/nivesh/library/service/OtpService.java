package com.nivesh.library.service;

/**
 * Interface for sending otp.
 *
 * @author Roshan
 */
public interface OtpService {

    /**
     * Sends otp in desired place
     */
    void send(String mobile, String otp);
}

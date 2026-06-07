package com.nivesh.library.service;

/**
 * Interface for sending OTP
 */
public interface OtpSender {

    /**
     * Sends OTP to user contact
     *
     * @param contact place where otp should be send
     * @param otp OTP to be sent
     */
    void send(String contact, String otp);
}

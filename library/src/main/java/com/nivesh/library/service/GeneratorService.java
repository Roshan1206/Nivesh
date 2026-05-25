package com.nivesh.library.service;

/**
 * Generate next number either based in sequence or random number.
 *
 * @author Roshan
 */
public interface GeneratorService {

    /**
     * Get next value from the sequence
     *
     * @param seq sequence name
     * @return next value
     */
    long generateNextSeqValue(String seq);

    /**
     * Generate otp for verification
     *
     * @return otp
     */
    String generateOtp();
}

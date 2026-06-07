package com.nivesh.library.service;

import com.nivesh.library.dto.request.OtpEntry;
import com.nivesh.library.entity.enums.OtpPurpose;
import com.nivesh.library.exception.CacheNotFoundException;
import com.nivesh.library.exception.OtpErrorCode;
import com.nivesh.library.exception.OtpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

/**
 * Service class for OTP generation and validation.
 */
@Slf4j
public class OtpCacheService {

    private static final String OTP_CACHE = "otp";

    /**
     * OTP cache
     */
    private final CacheManager cacheManager;

    /** Service used to create or inspect JWT values. */
    private final JwtTokenService jwtTokenService;

    /** Sender used to deliver OTP values. */
    private final OtpSender sender;

    /**
     * Generated random OTP
     */
    private final SecureRandom secureRandom;

    /**
     * Injecting required dependency using CI
     */
    public OtpCacheService(CacheManager cacheManager, JwtTokenService jwtTokenService,
                           OtpSender sender) {
        this.cacheManager = cacheManager;
        this.jwtTokenService = jwtTokenService;
        this.secureRandom = new SecureRandom();
        this.sender = sender;
    }

    /**
     * Generate and put otp in cache with unique key
     *
     * @param requestId otp request id
     */
    public void generateAndSendOtp(String requestId) {
        String email = jwtTokenService.extractEmail();
        generateAndSendOtp(requestId, email);
    }


    /**
     * Generates and sends an OTP to the specified email address based on the provided request ID.
     *
     * @param requestId The unique identifier for the request.
     * @param email The recipient's email address.
     */
    public void generateAndSendOtp(String requestId, String email) {
        int bound = (int) Math.pow(10, 6);
        int min = (int) Math.pow(10, 5);
        String plainOtp = String.format(
                "%0" + 6 + "d",
                secureRandom.nextInt(bound - min) + min
        );

        OtpEntry entry = new OtpEntry(plainOtp);
        log.info("OTP: {}", plainOtp);
        getCache().put(requestId, entry);
        sender.send(email, plainOtp);
    }

    /**
     * Validates and evict the otp from cache.
     *
     * @param requestId for which otp needs validation
     * @param submittedOtp user submitted otp
     * @throws OtpException for invalid otp
     */
    public void validateOtp(String requestId, String submittedOtp) {
//        Cache.ValueWrapper cache = getCache().get(requestId);
        OtpEntry entry = getCache().get(requestId, OtpEntry.class);

        if (entry == null) {
            throw new OtpException("OTP Expired", OtpErrorCode.EXPIRED);
        }


        if (!submittedOtp.equals(entry.getOtp())) {
            entry.incrementCount();
            if (entry.isMaxAttemptReached()) {
                getCache().evict(requestId);
                throw new OtpException("Max attempts reached. Request new OTP", OtpErrorCode.MAX_ATTEMPTS_EXCEEDED);
            }
            getCache().put(requestId, entry);
            int remaining = entry.getRemainingAttempts();
            throw new OtpException("Invalid OTP. " + remaining + " attempts remaining", OtpErrorCode.INVALID);
        }
        getCache().evict(requestId);
    }


    /**
     * Retrieves the application cache instance.
     *
     * @return Cache
     */
    private Cache getCache() {
        Cache cache = cacheManager.getCache(OtpCacheService.OTP_CACHE);
        if (cache == null) {
            throw new CacheNotFoundException(OTP_CACHE);
        }
        return cache;
    }

    /**
     * Builds a composite cache key combining request ID and OTP purpose.
     * Ensures unique keys for different OTP requests and purposes.
     *
     * @param requestId unique request identifier
     * @param purpose the OTP use case
     * @return combined cache key
     */
    public static String buildKey(String requestId, OtpPurpose purpose) {
        return requestId + ":" + purpose.name();
    }
}

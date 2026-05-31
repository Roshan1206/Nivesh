package com.nivesh.library.cache;

import com.nivesh.library.cache.properties.OtpCacheProperties;
import com.nivesh.library.configuration.cache.OtpCacheConfiguration;
import com.nivesh.library.dto.request.OtpEntry;
import com.nivesh.library.dto.response.OtpStore;
import com.nivesh.library.entity.enums.OtpPurpose;
import com.nivesh.library.exception.OtpErrorCode;
import com.nivesh.library.exception.OtpException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

/**
 * Service class for OTP generation and validation.
 */
public class OtpCacheService {

    /**
     * Encoder for encoding OTPs.
     */
    private final BCryptPasswordEncoder encoder;

    /**
     * OTP cache
     */
    private final Cache otpCache;

    /**
     * Properties to be used for cache
     */
    private final OtpCacheProperties properties;

    /**
     * Generated random OTP
     */
    private final SecureRandom secureRandom;

    /**
     * Injecting required dependency using CI
     */
    public OtpCacheService(@Qualifier("otpCacheManager") CacheManager cacheManager,
                           OtpCacheProperties properties) {
        this.otpCache = cacheManager.getCache(OtpCacheConfiguration.OTP_CACHE_NAME);
        this.properties = properties;
        this.encoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate and put otp in cache with unique key
     *
     * @param requestId otp request id
     * @param otpPurpose purpose for which the otp is generated
     * @return OtpStore containing requestId and OTP
     */
    public OtpStore generateOtp(String requestId, OtpPurpose otpPurpose) {
        int bound = (int) Math.pow(10, properties.getOtpLength());
        int min = (int) Math.pow(10, properties.getOtpLength() - 1);
        String plainOtp = String.format(
                "%0" + properties.getOtpLength() + "d",
                secureRandom.nextInt(bound - min) + min
        );

        String otpHash = encoder.encode(plainOtp);
        OtpEntry entry = new OtpEntry(otpHash, requestId, otpPurpose, properties.getTtlSeconds(), properties.getMaxAttempts());
        otpCache.put(buildKey(requestId, otpPurpose), entry);
        return new OtpStore(plainOtp, entry.getRequestId());
    }


    /**
     * Validates and evict the otp from cache.
     *
     * @param requestId for which otp needs validation
     * @param otpPurpose purpose for otp validation
     * @param submittedOtp user submitted otp
     * @throws OtpException for invalid otp
     */
    public void validateOtp(String requestId, OtpPurpose otpPurpose, String submittedOtp) {
        String key = buildKey(requestId, otpPurpose);
        OtpEntry entry = otpCache.get(key, OtpEntry.class);

        if (entry == null) {
            throw new OtpException("OTP Expired", OtpErrorCode.EXPIRED);
        }

        if (entry.isExpired()) {
            otpCache.evict(key);
            throw new OtpException("OTP Expired. Request new one.", OtpErrorCode.EXPIRED);
        }

        if (entry.isMaxAttemptReached()) {
            otpCache.evict(key);
            throw new OtpException("Max attempts reached. Request new OTP", OtpErrorCode.MAX_ATTEMPTS_EXCEEDED);
        }

        if (!encoder.matches(submittedOtp, entry.getOtpHash())) {
            entry.incrementCount();
            otpCache.put(key, entry);
            int remaining = entry.getRemainingAttempts();
            throw new OtpException("Invalid OTP. " + remaining + " attempts remaining", OtpErrorCode.INVALID);
        }
        otpCache.evict(key);
    }


    /**
     * Builds a composite cache key combining request ID and OTP purpose.
     * Ensures unique keys for different OTP requests and purposes.
     *
     * @param requestId unique request identifier
     * @param purpose the OTP use case
     * @return combined cache key
     */
    private String buildKey(String requestId, OtpPurpose purpose) {
        return requestId + ":" + purpose.name();
    }
}

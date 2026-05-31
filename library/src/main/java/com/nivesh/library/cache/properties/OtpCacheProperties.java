package com.nivesh.library.cache.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties needed for OTP.
 * Default value can be overridden via yml file.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nivesh.otp")
public class OtpCacheProperties {

    /**
     * Max time before Otp expires in seconds. default - 5 min
     */
    private int ttlSeconds = 300;

    /**
     * Max attempts for otp validation. default - 3
     */
    private int maxAttempts = 3;

    /**
     * Max otp can be stored in cache. default - 10,000
     */
    private int maxCacheSize = 100;

    /**
     * otp length. default - 6
     */
    private int otpLength = 6;
}

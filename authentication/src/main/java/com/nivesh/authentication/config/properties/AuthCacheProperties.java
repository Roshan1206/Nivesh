package com.nivesh.authentication.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nivesh.auth.cache")
public class AuthCacheProperties {

    /**
     * Max attempts for otp validation. default - 3
     */
    private int maxAttempts = 3;

    /**
     * Max otp can be stored in cache. default - 10,000
     */
    private int maxCacheSize = 10_000;

    /**
     * User locked out of their account
     */
    private int lockDurationMin = 60;
}

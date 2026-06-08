package com.nivesh.authentication.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cache limits and lockout settings used by authentication flows.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nivesh.auth.cache")
public class AuthCacheProperties {

    /**
     * Maximum failed login attempts before locking the account. Default: 3.
     */
    private int maxAttempts = 3;

    /**
     * Maximum entries stored across authentication caches. Default: 10,000.
     */
    private int maxCacheSize = 10_000;

    /**
     * Minutes an account remains locked after reaching the failed-login limit.
     */
    private int lockDurationInHour = 60;
}

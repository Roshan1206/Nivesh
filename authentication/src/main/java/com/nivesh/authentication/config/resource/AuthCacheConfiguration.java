package com.nivesh.authentication.config.resource;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nivesh.authentication.config.properties.AuthCacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class that wires auth cache configuration settings for the authentication module.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(AuthCacheProperties.class)
public class AuthCacheConfiguration {

    /** Cache that keeps pending registration requests until OTP verification completes. */
    public static final String REGISTER_CACHE_NAME = "register";

    /** Cache that tracks failed login attempts while an account can be locked. */
    public static final String LOGIN_CACHE_NAME = "login";

    /**
     * Creates the authentication cache manager used by registration and login flows.
     * Both caches share the same size limit and expiry window because their entries are short lived.
     */
    @Primary
    @Bean({"authCacheManager"})
    public CacheManager authCacheManager(AuthCacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(REGISTER_CACHE_NAME, LOGIN_CACHE_NAME);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                    .maximumSize(properties.getMaxCacheSize())
                    .expireAfterWrite(properties.getLockDurationMin(), TimeUnit.MINUTES)
                    .recordStats()
        );
        return cacheManager;
    }
}

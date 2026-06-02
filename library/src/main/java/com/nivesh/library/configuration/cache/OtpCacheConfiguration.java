package com.nivesh.library.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nivesh.library.cache.EmailOtpSender;
import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.cache.OtpSender;
import com.nivesh.library.annotation.EnableOtpCache;
import com.nivesh.library.cache.properties.OtpCacheProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class enabled by {@link EnableOtpCache}.
 * Configures beans required for sending and validating OTPs.
 */
@EnableCaching
@Configuration
@ConditionalOnBean(annotation = EnableOtpCache.class)
@EnableConfigurationProperties(OtpCacheProperties.class)
public class OtpCacheConfiguration {

    /** Name of the OTP cache shared by the cache manager and OTP service. */
    public static final String OTP_CACHE_NAME = "otpCache";

    /**
     * Creates a Caffeine cache manager for OTP storage with TTL and size limits.
     * Automatically evicts expired entries and maintains cache statistics.
     *
     * @param cacheProperties configuration properties for OTP cache
     * @return configured CacheManager
     */
    @Bean
    @ConditionalOnMissingBean(name = "otpCacheManager")
    public CacheManager otpCacheManager(OtpCacheProperties cacheProperties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(OTP_CACHE_NAME);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(cacheProperties.getTtlSeconds(), TimeUnit.SECONDS)
                        .maximumSize(cacheProperties.getMaxCacheSize())
                        .recordStats()
        );
        return cacheManager;
    }


    /**
     * Instantiates the OTP cache service for generating and validating OTPs.
     * Conditionally registers only if not provided by a consuming application.
     *
     * @param cacheManager the OTP cache manager
     * @param properties OTP configuration properties
     * @return OtpCacheService instance
     */
    @Bean
    @ConditionalOnMissingBean(OtpCacheService.class)
    public OtpCacheService otpCacheService(@Qualifier("otpCacheManager") CacheManager cacheManager,
                                           OtpCacheProperties properties) {
        return new OtpCacheService(cacheManager, properties);
    }


    /**
     * Provides an email-based OTP sender implementation.
     * Sends OTPs asynchronously with built-in retry mechanism.
     * Only created if not provided by the consuming application.
     *
     * @param mailSender Spring mail sender configured for SMTP
     * @return OtpSender implementation
     */
    @Bean
    @ConditionalOnMissingBean(EmailOtpSender.class)
    public OtpSender emailOtpSender(JavaMailSender mailSender) {
        return new EmailOtpSender(mailSender);
    }
}

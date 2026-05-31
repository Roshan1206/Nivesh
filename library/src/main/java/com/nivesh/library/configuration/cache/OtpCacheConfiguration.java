package com.nivesh.library.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nivesh.library.cache.EmailOtpSender;
import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.cache.OtpSender;
import com.nivesh.library.cache.annotation.EnableOtpCache;
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

    public static final String OTP_CACHE_NAME = "otpCache";

    /**
     * Create CaffeineCacheManager specifically for OTP cache using cache properties
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
     * Register Service class used for OTP
     */
    @Bean
    @ConditionalOnMissingBean(OtpCacheService.class)
    public OtpCacheService otpCacheService(@Qualifier("otpCacheManager") CacheManager cacheManager,
                                           OtpCacheProperties properties) {
        return new OtpCacheService(cacheManager, properties);
    }


    /**
     * Used for sending OTP via email
     */
    @Bean
    @ConditionalOnMissingBean(EmailOtpSender.class)
    public OtpSender emailOtpSender(JavaMailSender mailSender) {
        return new EmailOtpSender(mailSender);
    }
}

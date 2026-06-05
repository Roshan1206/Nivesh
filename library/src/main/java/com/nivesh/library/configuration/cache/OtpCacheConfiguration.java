package com.nivesh.library.configuration.cache;

import com.nivesh.library.configuration.EmailOtpSender;
import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.cache.OtpSender;
import com.nivesh.library.annotation.EnableOtpCache;
import com.nivesh.library.cache.properties.OtpCacheProperties;
import com.nivesh.library.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Configuration class enabled by {@link EnableOtpCache}.
 * Configures beans required for sending and validating OTPs.
 */
@EnableCaching
@Configuration
@ConditionalOnBean(annotation = EnableOtpCache.class)
@EnableConfigurationProperties(OtpCacheProperties.class)
public class OtpCacheConfiguration {

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
    public OtpCacheService otpCacheService(@Qualifier("cacheManager") CacheManager cacheManager, JwtTokenService jwtTokenService,
                                           OtpCacheProperties properties, OtpSender sender) {
        return new OtpCacheService(cacheManager, jwtTokenService, properties, sender);
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

package com.nivesh.library.cache.annotation;

import com.nivesh.library.configuration.cache.OtpCacheConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables OTP services to be used by consuming applications.
 * Creates {@link com.nivesh.library.cache.OtpCacheService}, {@link com.nivesh.library.cache.OtpSender} beans
 * using {@link OtpCacheConfiguration}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({OtpCacheConfiguration.class})
public @interface EnableOtpCache {
}

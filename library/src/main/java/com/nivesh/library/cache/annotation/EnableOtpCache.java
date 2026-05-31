package com.nivesh.library.cache.annotation;

import com.nivesh.library.configuration.cache.OtpCacheConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Meta-annotation to enable OTP generation and validation features.
 * Imports {@link OtpCacheConfiguration} to register OTP beans in the application context.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({OtpCacheConfiguration.class})
public @interface EnableOtpCache {
}

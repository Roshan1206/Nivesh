package com.nivesh.authentication.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Expiry settings for authentication tokens issued at each customer lifecycle stage.
 */
@ConfigurationProperties(prefix = "nivesh.auth.token")
public record TokenProperties(String onboardedExpiry, String accessExpiry, String refreshExpiry) {
}

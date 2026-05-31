package com.nivesh.authentication.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nivesh.auth.token")
public record TokenProperties(String onboardedExpiry, String accessExpiry, String refreshExpiry) {
}

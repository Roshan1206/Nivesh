package com.nivesh.library.configuration.audit;

import com.nivesh.library.service.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Configuration class for getting the current user email to be used in auditing.
 * Any request in which is user is not logged in like user registration or forgot password
 * the last modified or created by column would be "SYSTEM" indicating that this request has been raised by user itself.
 *
 * @author Roshan
 */
@Component("auditConfig")
public class AuditConfiguration implements AuditorAware<String> {

    /**
     * Used for getting the request and header value.
     */
    private final HttpServletRequest servletRequest;

    /**
     * Responsible for getting user email
     */
    private final JwtTokenService jwtTokenService;

    /**
     * Injecting required dependency via Constructor Injection
     */
    public AuditConfiguration(HttpServletRequest servletRequest, JwtTokenService jwtTokenService) {
        this.servletRequest = servletRequest;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Get current user email for token if authenticated else "SELF".
     * Used for auditing.
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        String authHeader = SecurityContextHolder.getContext().getAuthentication().getName();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.of("SYSTEM");
        }
        String token = authHeader.substring(7);
        return Optional.of(jwtTokenService.extractEmail(token));
    }
}

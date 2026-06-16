package com.nivesh.library.configuration.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
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
     * Get current user email for token if authenticated else "SELF".
     * Used for auditing.
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }
        String name = authentication.getName();
        return Optional.of(name);
    }
}

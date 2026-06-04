package com.nivesh.library.configuration.security;

import com.nivesh.library.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * Authorization manager that validates requests from internal microservices.
 * Verifies presence of required headers before granting access to internal endpoints.
 *
 * @author Roshan
 */
@Slf4j
public class InternalServiceAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        HttpServletRequest request = object.getRequest();
        log.info(Constants.IDEMPOTENCY_KEY + ": {}", request.getHeader(Constants.IDEMPOTENCY_KEY));
        String internalRole = request.getHeader(Constants.INTERNAL_ROLE_HEADER_NAME);
        String sourceService = request.getHeader(Constants.SOURCE_SERVICE_HEADER_NAME);

        // Both headers must be present and valid for access
        boolean isValid = Constants.INTERNAL_ROLE_HEADER_VALUE.equals(internalRole) &&
                sourceService != null && !sourceService.isBlank();
        return new AuthorizationDecision(isValid);
    }
}

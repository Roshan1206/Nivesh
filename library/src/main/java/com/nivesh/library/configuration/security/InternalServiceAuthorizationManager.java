package com.nivesh.library.configuration.security;

import com.nivesh.library.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * Authorization manager for validating all the internal endpoints.
 * will be authorized if required headers are present in request.
 *
 * @author Roshan
 */
public class InternalServiceAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        HttpServletRequest request = object.getRequest();
        String internalRole = request.getHeader(Constants.INTERNAL_ROLE_HEADER_NAME);
        String sourceService = request.getHeader(Constants.SOURCE_SERVICE_HEADER_NAME);

        boolean isValid = Constants.INTERNAL_ROLE_HEADER_VALUE.equals(internalRole) &&
                sourceService != null && !sourceService.isBlank();
        return new AuthorizationDecision(isValid);
    }
}

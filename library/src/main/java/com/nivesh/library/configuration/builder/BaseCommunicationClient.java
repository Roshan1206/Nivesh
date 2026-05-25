package com.nivesh.library.configuration.builder;

import com.nivesh.library.constant.Constants;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Base class for building communication using web client.
 *
 * @author Roshan
 */
@Component
public class BaseCommunicationClient {

    /**
     * Responsible for getting application name.
     */
    private final ApplicationContext context;

    /**
     * Responsible for building web client
     */
    private final WebClient.Builder builder;


    /**
     * Injecting required dependency via constructor injection.
     */
    public BaseCommunicationClient(ApplicationContext context,
                                   WebClient.Builder builder) {
        this.context = context;
        this.builder = builder;
    }

    /**
     * Creates new request using the user's token.
     * Add 2 internal headers in every request for validation.
     *
     * @param url service url
     * @return WebClient client
     */
    public WebClient create(String url) {
        return builder.baseUrl(url)
                .filter((req, next) -> {
                    ClientRequest.Builder reqBuilder = ClientRequest.from(req);
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                        String rawToken = jwtAuth.getToken().getTokenValue();
                        reqBuilder.header(HttpHeaders.AUTHORIZATION, rawToken);
                    }
                    reqBuilder.header(Constants.INTERNAL_ROLE_HEADER_NAME, Constants.INTERNAL_ROLE_HEADER_VALUE);
                    reqBuilder.header(Constants.SOURCE_SERVICE_HEADER_NAME, context.getApplicationName());
                    return next.exchange(reqBuilder.build());
                }).build();
    }
}

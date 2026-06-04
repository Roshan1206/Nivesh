package com.nivesh.library.configuration.builder;

import com.nivesh.library.constant.Constants;
import com.nivesh.library.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Base client for building inter-service HTTP communication using WebClient.
 * Automatically injects JWT tokens and internal service headers in all requests.
 *
 * @author Roshan
 */
@Component
public class BaseCommunicationClient {

    /** Application name from configuration */
    @Value("${spring.application.name}")
    private String name;

    /** WebClient builder for creating configured HTTP clients */
    private final WebClient.Builder builder;


    /**
     * Injecting required dependency via constructor injection.
     */
    public BaseCommunicationClient(WebClient.Builder builder) {
        this.builder = builder;
    }

    /**
     * Constructs a new WebClient configured to:
     * - Forward the current user's JWT token
     * - Include internal service headers for microservice authentication
     *
     * @param url the base URL for the target service
     * @return configured WebClient instance
     */
    public WebClient create(String url) {
        return builder.baseUrl(url)
                .filter((req, next) -> {
                    ClientRequest.Builder reqBuilder = ClientRequest.from(req);
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                        String rawToken = jwtAuth.getToken().getTokenValue();
                        reqBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);
                    }
                    reqBuilder.header(Constants.INTERNAL_ROLE_HEADER_NAME, Constants.INTERNAL_ROLE_HEADER_VALUE);
                    reqBuilder.header(Constants.SOURCE_SERVICE_HEADER_NAME, name);
                    return next.exchange(reqBuilder.build());
                }).build();
    }
}

package com.nivesh.customer.service.client;

import com.nivesh.library.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Client abstraction for communicating with dependent services from the customer module.
 */
@Slf4j
@Component
public class AuthServerClient {

    /** WebClient configured for authentication-service calls. */
    private final WebClient webClient;

    /**
     * Creates an auth-server client for the configured base URL.
     */
    public AuthServerClient(@Qualifier("authClient") WebClient webClient) {
        this.webClient = webClient;
    }


    /**
     * Updates the user's status based on the provided user ID and status.
     *
     * @param userId The ID of the user to update.
     * @param status The new status for the user.
     * @return A success message upon successful update, or an error message if the update fails.
     */
    public String updateUserStatus(String userId, String status) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/internal/{userId}/{status}")
                        .build(userId, status))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof WebClientResponseException ex
                                && ex.getStatusCode().is5xxServerError())
                        .onRetryExhaustedThrow((spec, signal) ->
                                new ServiceUnavailableException(signal.failure().getMessage(), "auth service")))
                .block();
    }
}

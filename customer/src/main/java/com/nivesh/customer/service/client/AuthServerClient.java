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

@Slf4j
@Component
public class AuthServerClient {

    private final WebClient webClient;

    public AuthServerClient(@Qualifier("authClient") WebClient webClient) {
        this.webClient = webClient;
    }

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

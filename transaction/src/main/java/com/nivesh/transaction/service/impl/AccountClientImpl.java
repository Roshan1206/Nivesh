package com.nivesh.transaction.service.impl;

import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountValidationResponse;
import com.nivesh.library.exception.ServiceUnavailableException;
import com.nivesh.transaction.service.AccountsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

/**
 * Class that supports account client impl behavior in the transaction module.
 */
@Slf4j
@Service
public class AccountClientImpl implements AccountsClient {

    private static final String ACCOUNTS = "/accounts/internal";

    /** Web client value used by this component. */
    private final WebClient webClient;

    /**
     * Injects the account communication client used to call the account service.
     */
    public AccountClientImpl(@Qualifier("accountClient") WebClient client) {
        this.webClient = client;
    }


    /**
     * Validates an account based on the provided transaction request.
     *
     * @param request The transaction request containing account details.
     * @return An AccountValidationResponse object indicating the validation result.
     */
    @Override
    public AccountValidationResponse validateAccount(TransactionRequest request) {
        return webClient.post()
                .uri(ACCOUNTS + "/validate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AccountValidationResponse.class)
                .retryWhen(doRetry())
                .block();
    }


    /**
     * Creates a RetryBackoffSpec with an initial delay of 1 second and exponential backoff.
     *
     * @return RetryBackoffSpec
     */
    @NonNull
    private static RetryBackoffSpec doRetry() {
        return Retry.backoff(3, Duration.ofSeconds(2))
                .maxBackoff(Duration.ofSeconds(10))
                .filter(throwable -> throwable instanceof WebClientResponseException exception &&
                        exception.getStatusCode().is5xxServerError())
                .onRetryExhaustedThrow((spec, signal) -> {
                    log.error("Account service is down");
                    return new ServiceUnavailableException(signal.failure().getMessage(), "Accounts service");
                });
    }
}

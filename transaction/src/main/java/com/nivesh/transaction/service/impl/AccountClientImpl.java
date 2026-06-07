package com.nivesh.transaction.service.impl;

import com.nivesh.library.constant.Constants;
import com.nivesh.library.dto.request.AmountTransactionRequest;
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
import java.util.UUID;

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
     * Processes a debit transaction for the specified account.
     *
     * @param accountId The unique identifier of the account to debit.
     * @param idempotencyKey A key used to prevent duplicate transactions.
     * @param request The transaction request containing the amount and other details.
     */
    @Override
    public void debit(UUID accountId, String idempotencyKey, AmountTransactionRequest request) {
        webClient.post()
                .uri(uri -> uri
                        .path(ACCOUNTS + "/{accountId}/debit")
                        .build(accountId))
                .header(Constants.IDEMPOTENCY_KEY, idempotencyKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(doRetry())
                .block();
    }


    /**
     * Credits money to an account based on the provided transaction request.
     *
     * @param accountId The unique identifier of the account to credit.
     * @param idempotencyKey A key used to prevent duplicate transactions.
     * @param request The transaction request containing the amount and other details.
     */
    @Override
    public void credit(UUID accountId, String idempotencyKey, AmountTransactionRequest request) {
        webClient.post()
                .uri(uri -> uri
                        .path(ACCOUNTS + "/{accountId}/credit")
                        .build(accountId))
                .header(Constants.IDEMPOTENCY_KEY, idempotencyKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
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

package com.nivesh.transaction.service;

import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.exception.ServiceUnavailableException;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@EnableScheduling
@Component
public class TransactionScheduler {

    @Value("${nivesh.scheduler.credit-retry.stuck-threshold-minutes}")
    private String creditThreshold;

    @Value("${nivesh.scheduler.credit-retry.max-retry-count}")
    private String creditMaxRetry;

    @Value("${nivesh.scheduler.compensation.stuck-threshold-minutes}")
    private String compensationThreshold;

    @Value("${nivesh.scheduler.compensation.max-retry-count}")
    private String compensationMaxRetry;

    private final AccountsClient accountsClient;

    private final TransactionProducer transactionProducer;

    private final TransactionRepository transactionRepository;

    public TransactionScheduler(AccountsClient accountsClient, TransactionProducer transactionProducer,
                                TransactionRepository transactionRepository) {
        this.accountsClient = accountsClient;
        this.transactionProducer = transactionProducer;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(fixedDelayString = "${nivesh.scheduler.credit-retry.fixed-delay-ms}")
    public void retryCreditTransaction() {
        log.debug("Credit retry scheduler started");

        if (!accountsClient.isAccountServiceAvailable()){
            log.error("Credit retry scheduler skipped. Account service unavailable");
            return;
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(Integer.parseInt(creditThreshold));
        List<Transaction> transactions = transactionRepository.findStuckCreditTransaction(threshold, Integer.parseInt(creditMaxRetry));

        if (transactions.isEmpty()){
            log.debug("No credit transaction available for retry");
            return;
        }

        log.debug("Found {} transaction(s) to retry credit. Initiating now", transactions.size());
        for (Transaction txn: transactions){
            retryCredit(txn);
        }
    }


    @Scheduled(fixedDelayString = "${nivesh.scheduler.compensation.fixed-delay-ms}")
    public void retryCompensationTransaction() {
        log.debug("Compensation retry scheduler started");

        if (!accountsClient.isAccountServiceAvailable()){
            log.error("Compensation retry scheduler skipped. Account service unavailable");
            return;
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(Integer.parseInt(compensationThreshold));
        List<Transaction> transactions = transactionRepository.findStuckCompensateTransaction(
                threshold, Integer.parseInt(compensationMaxRetry));

        if (transactions.isEmpty()){
            log.debug("No Compensation transaction available for retry");
            return;
        }

        log.debug("Found {} transaction(s) to retry Compensation. Initiating now", transactions.size());
        for (Transaction txn: transactions){
            retryCompensation(txn);
        }
    }


    private void retryCredit(Transaction transaction) {
        String ref = transaction.getReferenceNumber();
        int count = transaction.getCreditRetryCount() + 1;
        log.trace("Retrying credit. Reference number: {}, Retry number: {}", ref, count);

        try {
            AmountTransactionRequest request = new AmountTransactionRequest(transaction.getAmount());
            accountsClient.credit(transaction.getDestinationAccountId(),
                    transaction.getIdempotencyKey(), request);
            transaction.setStatus(TransactionStatus.POSTED);
            transactionRepository.save(transaction);
            log.trace("Credit completed. Reference number: {}", ref);
        } catch (Exception e) {
            transaction.setCreditRetryCount(count);

            if (isServiceUnavailable(e)) {
                log.trace("Credit retry failed. Service unavailable in mid. Reference number: {}", ref);
                transaction.setStatus(TransactionStatus.CREDIT_RETRY);
                transactionRepository.save(transaction);
            }

            log.trace("Credit transaction failed. Initiating compensation. Reference number: {}", ref);
            transaction.setStatus(TransactionStatus.COMPENSATE_INITIATED);
            transactionRepository.save(transaction);
        }
    }


    private void retryCompensation(Transaction transaction) {
        String ref = transaction.getReferenceNumber();

        if (transaction.getCompensateRetryCount() >= Integer.parseInt(compensationMaxRetry)) {
            log.trace("Compensation retry has been exhausted. Required Manual intervention. Reference number = {}", ref);
            transaction.setStatus(TransactionStatus.MANUAL_REVIEW);
            transactionRepository.save(transaction);
            transactionProducer.publishDeadLetter(ref, "Compensation exhausted after " +
                    transaction.getCompensateRetryCount());
        }

        int count = transaction.getCompensateRetryCount() + 1;
        transaction.setCompensateRetryCount(count);
        log.trace("Compensation started. Reference number: {}, retry number: {}", ref, count);

        try {
            CompensateRequestEvent event = CompensateRequestEvent.builder()
                    .sourceAccountId(transaction.getSourceAccountId())
                    .amount(transaction.getAmount())
                    .idempotencyKey(transaction.getIdempotencyKey())
                    .referenceNumber(transaction.getReferenceNumber())
                    .build();
            transactionProducer.publishCompensateRequest(event);
            transactionRepository.save(transaction);
        } catch (Exception e) {
            if (isServiceUnavailable(e)) {
                transactionRepository.save(transaction);
                log.warn("Compensation skipped. Service unavailable. Reference number: {}", ref);
                return;
            }
            transactionRepository.save(transaction);
            log.trace("Compensation failed. Reference number: {}, message: {}", ref, e.getMessage());
        }
    }


    private boolean isServiceUnavailable(Exception ex) {
        if (ex instanceof ServiceUnavailableException) {
            return true;
        }
        if (ex instanceof WebClientResponseException resEx) {
            return resEx.getStatusCode().is5xxServerError();
        }
        return ex.getCause() instanceof ConnectException ||
                ex.getMessage() != null && ex.getMessage().contains("Connection Refused");
    }
}

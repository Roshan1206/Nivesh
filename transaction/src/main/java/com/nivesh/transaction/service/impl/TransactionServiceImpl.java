package com.nivesh.transaction.service.impl;

import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.constant.CacheConstants;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountValidationResponse;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.entity.enums.OtpPurpose;
import com.nivesh.library.exception.SessionExpiredException;
import com.nivesh.transaction.dto.response.TransactionResponse;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionChannel;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.entity.enums.TransactionType;
import com.nivesh.transaction.exception.TransactionFailedException;
import com.nivesh.transaction.exception.TransactionNotFoundException;
import com.nivesh.transaction.repository.TransactionRepository;
import com.nivesh.transaction.service.AccountsClient;
import com.nivesh.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation that coordinates transaction business logic for transaction operations.
 */
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    /** Client used to debit, credit, and validate accounts. */
    private final AccountsClient accountsClient;

    /** Cache value used by this component. */
    private final Cache cache;

    /** Service used to create and validate OTP cache entries. */
    private final OtpCacheService otpCacheService;

    /** Repository used to persist transaction records. */
    private final TransactionRepository transactionRepository;

    /** Service used to resolve transaction configuration. */
    private final TransactionConfigServiceImpl transactionConfigService;

    /**
     * Injects repositories and clients required to process transactions.
     */
    public TransactionServiceImpl(AccountsClient accountsClient, CacheManager cacheManager,
                                  OtpCacheService otpCacheService, TransactionRepository transactionRepository,
                                  TransactionConfigServiceImpl transactionConfigService) {
        this.accountsClient = accountsClient;
        this.cache = cacheManager.getCache(CacheConstants.TRANSACTION_CACHE_NAME);
        this.otpCacheService = otpCacheService;
        this.transactionRepository = transactionRepository;
        this.transactionConfigService = transactionConfigService;
    }

    @Transactional
    @Override
    public OtpResponse initiateTransaction(String idempotencyKey, TransactionRequest transactionRequest) {
        AccountValidationResponse validationResponse = accountsClient.validateAccount(transactionRequest);
        String requestId = UUID.randomUUID().toString();
        otpCacheService.generateOtp(requestId, OtpPurpose.TRANSACTION);
        String key = OtpCacheService.buildKey(requestId, OtpPurpose.TRANSACTION);
        TransactionTypeConfig typeConfig = transactionConfigService.getTransactionType(TransactionType.DEBIT);
        Transaction transaction = buildTransaction(idempotencyKey, validationResponse, transactionRequest);
        transaction.setTypeConfig(typeConfig);
        Transaction savedTxn = transactionRepository.save(transaction);
        cache.put(key, savedTxn.getId());
        return new OtpResponse(requestId);
    }

    @Override
    public TransactionResponse startTransaction(String requestId, String otp) {
        String key = OtpCacheService.buildKey(requestId, OtpPurpose.TRANSACTION);
        otpCacheService.validateOtp(requestId, OtpPurpose.TRANSACTION, otp);
        UUID txnId = cache.get(key, UUID.class);
        if (txnId == null) {
            throw new SessionExpiredException(HttpStatus.REQUEST_TIMEOUT, "Session has been expired");
        }
        Transaction transaction = getTransaction(txnId);
        performDebitTransaction(transaction);
        performCreditTransaction(transaction, null);

        return new TransactionResponse(transaction.getReferenceNumber(), HttpStatus.OK, "Transaction Completed");
    }

    private void performCreditTransaction(Transaction transaction, UUID compAccountId) {
        UUID accountId = compAccountId != null ? compAccountId : transaction.getDestinationAccountId();
        String idempotencyKey = transaction.getIdempotencyKey();
        String referenceNumber = transaction.getReferenceNumber();
        AmountTransactionRequest request = new AmountTransactionRequest(transaction.getAmount());
        try {
            accountsClient.credit(accountId, idempotencyKey, request);
            log.debug("Credit completed. Reference Number : {}", referenceNumber);
            updateStatus(transaction, TransactionStatus.POSTED);
        } catch (Exception exception) {
            log.error("Transaction failed at credit stage. Reference number : {}, message : {}", referenceNumber, exception.getMessage());
            updateStatus(transaction, TransactionStatus.FAILED);
            compensateTransaction(transaction);
            throw new TransactionFailedException(exception.getMessage());
        }
    }

    private void performDebitTransaction(Transaction transaction) {
        String idempotencyKey = transaction.getIdempotencyKey();
        String referenceNumber = transaction.getReferenceNumber();
        AmountTransactionRequest request = new AmountTransactionRequest(transaction.getAmount());
        try {
            accountsClient.debit(transaction.getSourceAccountId(), idempotencyKey, request);
            log.debug("Debit completed. Reference Number : {}", referenceNumber);
            updateStatus(transaction, TransactionStatus.DEBIT_SUCCESS);
        } catch (Exception exception) {
            log.error("Transaction failed at debit stage. Reference number : {}, message : {}", referenceNumber, exception.getMessage());
            updateStatus(transaction, TransactionStatus.FAILED);
            throw new TransactionFailedException(exception.getMessage());
        }
    }

    private void compensateTransaction(Transaction transaction) {
        updateStatus(transaction, TransactionStatus.COMPENSATE_INITIATED);
        performCreditTransaction(transaction, transaction.getSourceAccountId());
    }

    private void updateStatus(Transaction transaction, TransactionStatus transactionStatus) {
        transaction.setStatus(transactionStatus);
        transactionRepository.save(transaction);
    }

    private Transaction getTransaction(UUID txnId) {
        return transactionRepository.findById(txnId).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found")
        );
    }

    private static Transaction buildTransaction(String idempotencyKey, AccountValidationResponse validationResponse,
                                                TransactionRequest request) {
        String s = String.valueOf(System.currentTimeMillis());
        int length = Math.min(s.length(), 8);
        String referenceNumber = LocalDate.now() + s.substring(length);
        return Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .referenceNumber(referenceNumber)
                .sourceAccountId(validationResponse.getSourceAccountId())
                .destinationAccountId(validationResponse.getDestinationAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionChannel(TransactionChannel.API)
                .transactionType(TransactionType.DEBIT)
                .initiatedBy(validationResponse.getSourceAccountId())
                .createdAt(LocalDateTime.now())
                .externalPartyRef(null)
                .status(TransactionStatus.INITIATED)
                .build();
    }
}

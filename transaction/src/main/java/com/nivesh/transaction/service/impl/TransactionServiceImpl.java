package com.nivesh.transaction.service.impl;

import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountValidationResponse;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.exception.CacheNotFoundException;
import com.nivesh.library.exception.SessionExpiredException;
import com.nivesh.library.service.OtpCacheService;
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

    /** Transaction cache name */
    private static final String TRANSACTION_CACHE = "transaction";

    /** Client used to debit, credit, and validate accounts. */
    private final AccountsClient accountsClient;

    /** Cache value used by this component. */
    private final CacheManager cacheManager;

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
        this.cacheManager = cacheManager;
        this.otpCacheService = otpCacheService;
        this.transactionRepository = transactionRepository;
        this.transactionConfigService = transactionConfigService;
    }

    /**
     * Initiate transaction after validating accounts from accounts and source account balance.
     * Actual transaction will start after OTP validation.
     * Build and cache the transaction object
     */
    @Transactional
    @Override
    public OtpResponse initiateTransaction(String idempotencyKey, TransactionRequest transactionRequest) {
        AccountValidationResponse validationResponse = accountsClient.validateAccount(transactionRequest);
        String requestId = UUID.randomUUID().toString();
        otpCacheService.generateAndSendOtp(requestId);
        TransactionTypeConfig typeConfig = transactionConfigService.getTransactionType(TransactionType.DEBIT);
        Transaction transaction = buildTransaction(idempotencyKey, validationResponse, transactionRequest);
        transaction.setTypeConfig(typeConfig);
        getCache().put(requestId, transaction);
        return new OtpResponse(requestId);
    }


    /**
     * Start the actual transaction after OTP validation.
     * Retrieve the transaction from cache and process.
     */
    @Transactional
    @Override
    public TransactionResponse startTransaction(String requestId, String otp) {
        otpCacheService.validateOtp(requestId, otp);
        Transaction transaction = getCache().get(requestId, Transaction.class);
        if (transaction == null) {
            throw new SessionExpiredException(HttpStatus.REQUEST_TIMEOUT, "Session has been expired");
        }
        performDebitTransaction(transaction);
        performCreditTransaction(transaction);
        persistAndCache(transaction);
        return new TransactionResponse(transaction.getReferenceNumber(), HttpStatus.OK, "Transaction Completed");
    }


    /** Perform credit operation from account. */
    private void performCreditTransaction(Transaction transaction) {
        UUID accountId = transaction.getDestinationAccountId();
        String idempotencyKey = transaction.getIdempotencyKey();
        String referenceNumber = transaction.getReferenceNumber();
        AmountTransactionRequest request = new AmountTransactionRequest(transaction.getAmount());
        try {
            accountsClient.credit(accountId, idempotencyKey, request);
            log.debug("Credit completed. Reference Number : {}", referenceNumber);
            transaction.setStatus(TransactionStatus.POSTED);
        } catch (Exception exception) {
            log.error("Transaction failed at credit stage. Reference number : {}, message : {}", referenceNumber, exception.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            persistAndCache(transaction);
            compensateTransaction(transaction);
            throw new TransactionFailedException(exception.getMessage());
        }
    }


    /** Perform debit operation from account. */
    private void performDebitTransaction(Transaction transaction) {
        String idempotencyKey = transaction.getIdempotencyKey();
        String referenceNumber = transaction.getReferenceNumber();
        AmountTransactionRequest request = new AmountTransactionRequest(transaction.getAmount());
        try {
            accountsClient.debit(transaction.getSourceAccountId(), idempotencyKey, request);
            log.debug("Debit completed. Reference Number : {}", referenceNumber);
            transaction.setStatus(TransactionStatus.DEBIT_SUCCESS);
        } catch (Exception exception) {
            log.error("Transaction failed at debit stage. Reference number : {}, message : {}", referenceNumber, exception.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            throw new TransactionFailedException(exception.getMessage());
        }
    }

    /** Starts the compensation after failed credit */
    private void compensateTransaction(Transaction transaction) {
        persistAndCache(transaction);
        TransactionTypeConfig typeConfig = transactionConfigService.getTransactionType(TransactionType.REVERSAL);
        AccountValidationResponse validationResponse =
                new AccountValidationResponse(transaction.getDestinationAccountId(), transaction.getSourceAccountId(),
                        null, true);
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(transaction.getAmount());
        transaction.setDescription("Credit for failed transaction. Reference number : " + transaction.getReferenceNumber());
        Transaction compensateTxn = buildTransaction(transaction.getIdempotencyKey(), validationResponse, transactionRequest);
        compensateTxn.setTypeConfig(typeConfig);
        performCreditTransaction(compensateTxn);
    }


    /** Save transaction in both DB and cache */
    private void persistAndCache(Transaction transaction) {
        Transaction savedTxn = transactionRepository.save(transaction);
        getCache().put(savedTxn.getReferenceNumber(), savedTxn);
    }

    /** Get transaction by transaction id */
    private Transaction getTransaction(UUID txnId) {
        return transactionRepository.findById(txnId).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found")
        );
    }


    /** Build the transaction object */
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

    /**
     * Get Transaction cache
     */
    private Cache getCache() {
        Cache cache = cacheManager.getCache(TRANSACTION_CACHE);
        if (cache == null) {
            throw new CacheNotFoundException(TRANSACTION_CACHE);
        }
        return cache;
    }
}

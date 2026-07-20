package com.nivesh.transaction.service.impl;

import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountValidationResponse;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.exception.CacheNotFoundException;
import com.nivesh.library.exception.SessionExpiredException;
import com.nivesh.library.service.OtpCacheService;
import com.nivesh.transaction.dto.PendingTransaction;
import com.nivesh.transaction.dto.response.TransactionResponse;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionChannel;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.entity.enums.TransactionType;
import com.nivesh.transaction.exception.TransactionNotFoundException;
import com.nivesh.transaction.repository.TransactionRepository;
import com.nivesh.transaction.service.AccountsClient;
import com.nivesh.transaction.service.OutboxEventService;
import com.nivesh.transaction.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    private final OutboxEventService outboxEventService;

    /** Repository used to persist transaction records. */
    private final TransactionRepository transactionRepository;

    /** Service used to resolve transaction configuration. */
    private final TransactionConfigServiceImpl transactionConfigService;

    /**
     * Injects repositories and clients required to process transactions.
     */
    public TransactionServiceImpl(AccountsClient accountsClient, CacheManager cacheManager, OutboxEventService outboxEventService,
                                  OtpCacheService otpCacheService, TransactionRepository transactionRepository,
                                  TransactionConfigServiceImpl transactionConfigService) {
        this.accountsClient = accountsClient;
        this.cacheManager = cacheManager;
        this.otpCacheService = otpCacheService;
        this.outboxEventService = outboxEventService;
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
        TransactionTypeConfig typeConfig = transactionConfigService.getTransactionType(TransactionType.TRANSFER);
        PendingTransaction pendingTxn = new PendingTransaction(
                validationResponse.getSourceAccountId(),
                validationResponse.getDestinationAccountId(),
                idempotencyKey,
                transactionRequest.getAmount(),
                typeConfig.getTypeCode(),
                transactionRequest.getDescription()
        );
        getCache().put(requestId, pendingTxn);
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
        PendingTransaction pendingTransaction = getCache().get(requestId, PendingTransaction.class);
        getCache().evict(requestId);
        if (pendingTransaction == null) {
            throw new SessionExpiredException(HttpStatus.REQUEST_TIMEOUT, "Session has been expired");
        }
        TransactionTypeConfig typeConfig = transactionConfigService.getTransactionType(
                TransactionType.valueOf(pendingTransaction.getTypeCode()));
        Transaction transaction = buildTransaction(pendingTransaction, typeConfig);
        this.updateTransaction(transaction);
        outboxEventService.publishTransferRequested(transaction);
        TransactionResponse response = new TransactionResponse(transaction.getReferenceNumber(), HttpStatus.OK, "Transaction Completed.");
        getCache().put(transaction.getReferenceNumber(), response);
        log.debug("Transaction has been processed. Reference number: {}", transaction.getReferenceNumber());
        return response;
    }


    /**
     * Get transaction info using reference number
     */
    @Override
    public Transaction getTransactionByRefNo(String refNo) {
        return transactionRepository.findByReferenceNumber(refNo).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found with id : " + refNo)
        );
    }


    /**
     * save updated transaction
     */
    @Override
    public void updateTransaction(Transaction txn) {
        transactionRepository.save(txn);
    }

    /**
     *  Build the transaction object with all the required metadata
     */
    private static Transaction buildTransaction(PendingTransaction transaction, TransactionTypeConfig typeConfig) {
        return Transaction.builder()
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceNumber(generateReferenceNumber())
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .amount(transaction.getAmount())
                .typeConfig(typeConfig)
                .description(transaction.getDescription())
                .transactionChannel(TransactionChannel.API)
                .transactionType(TransactionType.DEBIT)
                .initiatedBy(transaction.getSourceAccountId())
                .createdAt(LocalDateTime.now())
                .externalPartyRef(null)
                .status(TransactionStatus.INITIATED)
                .build();
    }


    /**
     * Generate a unique reference number consisting of 20 digits where first 17 are time down to ms and last 3 are random number.
     * This ensures a max of 1000 unique number per millisecond.
     */
    private static String generateReferenceNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return timestamp + random;
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

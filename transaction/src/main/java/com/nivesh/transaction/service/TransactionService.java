package com.nivesh.transaction.service;

import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.transaction.dto.response.TransactionResponse;
import com.nivesh.transaction.entity.Transaction;

/**
 * Service contract for transaction business logic related to transaction operations.
 */
public interface TransactionService {

    /**
     * Initiate transaction for account
     *
     * @param idempotencyKey unique key per transaction
     * @param transactionRequest transaction details
     * @return OTP request id
     */
    OtpResponse initiateTransaction(String idempotencyKey, TransactionRequest transactionRequest);


    /**
     * Start transaction
     *
     * @param requestId otp request id
     * @param otp submitted otp
     * @return reference number and status for future use
     */
    TransactionResponse startTransaction(String requestId, String otp);

    Transaction getTransactionByRefNo(String refNo);

    void markTransactionPosted(Transaction txn);
}

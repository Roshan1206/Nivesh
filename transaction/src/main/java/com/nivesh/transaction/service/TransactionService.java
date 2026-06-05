package com.nivesh.transaction.service;

import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.transaction.dto.response.TransactionResponse;

import java.util.UUID;

/**
 * Service contract for transaction business logic related to transaction operations.
 */
public interface TransactionService {

    OtpResponse initiateTransaction(String idempotencyKey, TransactionRequest transactionRequest);

    TransactionResponse startTransaction(String requestId, String otp);
}

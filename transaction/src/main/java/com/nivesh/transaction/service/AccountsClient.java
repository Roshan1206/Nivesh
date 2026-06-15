package com.nivesh.transaction.service;

import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountValidationResponse;

import java.util.UUID;

/**
 * Client abstraction for communicating with dependent services from the transaction module.
 */
public interface AccountsClient {

    AccountValidationResponse validateAccount(TransactionRequest request);
}

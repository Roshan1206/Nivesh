package com.nivesh.transaction.service;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionType;

/**
 * Service contract for transaction business logic related to transaction config operations.
 */
public interface TransactionConfigService {

    TransactionTypeConfig getTransactionType(TransactionType type);
}
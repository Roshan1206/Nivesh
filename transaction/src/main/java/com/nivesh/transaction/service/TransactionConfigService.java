package com.nivesh.transaction.service;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionType;

public interface TransactionConfigService {

    TransactionTypeConfig getTransactionType(TransactionType type);
}
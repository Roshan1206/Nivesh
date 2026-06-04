package com.nivesh.transaction.service.impl;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionType;
import com.nivesh.transaction.exception.TransactionNotFoundException;
import com.nivesh.transaction.repository.TransactionTypeConfigRepository;
import com.nivesh.transaction.service.TransactionConfigService;
import org.springframework.stereotype.Service;

@Service
public class TransactionConfigServiceImpl implements TransactionConfigService {

    private final TransactionTypeConfigRepository repository;

    public TransactionConfigServiceImpl(TransactionTypeConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransactionTypeConfig getTransactionType(TransactionType type) {
        return repository.findById(type.name()).orElseThrow(
                () -> new TransactionNotFoundException("Transaction type not found")
        );
    }
}

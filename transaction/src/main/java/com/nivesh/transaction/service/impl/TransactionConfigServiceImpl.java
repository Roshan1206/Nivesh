package com.nivesh.transaction.service.impl;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.entity.enums.TransactionType;
import com.nivesh.transaction.exception.TransactionNotFoundException;
import com.nivesh.transaction.repository.TransactionTypeConfigRepository;
import com.nivesh.transaction.service.TransactionConfigService;
import org.springframework.stereotype.Service;

/**
 * Service implementation that coordinates transaction business logic for transaction config operations.
 */
@Service
public class TransactionConfigServiceImpl implements TransactionConfigService {

    /** Repository value used by this component. */
    private final TransactionTypeConfigRepository repository;

    /**
     * Injects repositories used to manage transaction configuration.
     */
    public TransactionConfigServiceImpl(TransactionTypeConfigRepository repository) {
        this.repository = repository;
    }


    /**
     * Retrieves the configuration for a specific transaction type.
     *
     * @param type The transaction type to retrieve the configuration for.
     * @return The TransactionTypeConfig object corresponding to the given transaction type.
     */
    @Override
    public TransactionTypeConfig getTransactionType(TransactionType type) {
        return repository.findById(type.name()).orElseThrow(
                () -> new TransactionNotFoundException("Transaction type not found")
        );
    }
}

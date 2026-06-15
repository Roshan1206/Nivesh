package com.nivesh.transaction.service.impl;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import com.nivesh.transaction.exception.TransactionTypeConfigNotFoundException;
import com.nivesh.transaction.repository.TransactionTypeConfigRepository;
import com.nivesh.transaction.service.TransactionTypeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionTypeConfigServiceImpl implements TransactionTypeConfigService {

    private final TransactionTypeConfigRepository configRepository;

    public TransactionTypeConfigServiceImpl(TransactionTypeConfigRepository configRepository) {
        this.configRepository = configRepository;
    }


    @Override
    public TransactionTypeConfig getTransactionTypeConfig(String typeCode) {
        return configRepository.findById(typeCode).orElseThrow(
                () -> new TransactionTypeConfigNotFoundException("TransactionTypeConfig not found for given code : " + typeCode)
        );
    }
}

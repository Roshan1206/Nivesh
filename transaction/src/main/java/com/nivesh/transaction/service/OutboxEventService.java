package com.nivesh.transaction.service;

import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.OutboxStatus;

public interface OutboxEventService {

    void save(String aggregateId, OutboxStatus status);

    void publishTransferRequested(Transaction transaction);

    void publishDebitRequested(Transaction transaction);

    void publishCreditRequested(Transaction transaction);

    void publishCompensateRequested(Transaction transaction);
}

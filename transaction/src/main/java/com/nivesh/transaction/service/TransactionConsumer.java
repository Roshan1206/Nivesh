package com.nivesh.transaction.service;

import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateResultEvent;
import com.nivesh.library.dto.event.TransferResultEvent;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.OutboxStatus;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class TransactionConsumer {

    private final JournalEntryService journalEntryService;

    private final OutboxEventService outboxEventService;

    private final TransactionService transactionService;

    public TransactionConsumer(JournalEntryService journalEntryService, OutboxEventService outboxEventService,
                               TransactionService transactionService) {
        this.journalEntryService = journalEntryService;
        this.outboxEventService = outboxEventService;
        this.transactionService = transactionService;
    }

    @Transactional
    @KafkaListener(
            topics = KafkaTopics.TRANSFER_RESULT,
            groupId = "transaction-group",
            containerFactory = "transferResultListenerFactory")
    public void onTransferResult(TransferResultEvent event, Acknowledgment acknowledgement) {
        String ref = event.getTransferRequest().getReferenceNumber();
        if (!event.isSuccess()) {
            log.error("Transfer failed. Ref no: {}", ref);
            acknowledgement.acknowledge();
            return;
        }
        Transaction txn = transactionService.getTransactionByRefNo(ref);
        try {
            journalEntryService.writeLedger(event);
            outboxEventService.save(event.getTransferRequest().getReferenceNumber(), OutboxStatus.PUBLISHED);
            txn.setStatus(TransactionStatus.POSTED);
            log.trace("Transfer successful. Reference number: {}", ref);
        } catch (Exception e) {
            txn.setStatus(TransactionStatus.FAILED);
            log.error("Error processing writing ledger entry for transfer request");
        }
        transactionService.updateTransaction(txn);
        acknowledgement.acknowledge();
    }

    @Transactional
    @KafkaListener(
            topics = KafkaTopics.COMPENSATE_SUCCESS,
            groupId = "transaction-group",
            containerFactory = "compensateRequestListenerFactory")
    public void onCompensationSuccess(CompensateResultEvent event, Acknowledgment acknowledgement) {
        String ref = event.getReferenceNumber();
        log.trace("Compensation successful. Reference number: {}", ref);
        Transaction transaction = transactionService.getTransactionByRefNo(ref);
        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            log.trace("Duplicate compensation. Reference number: {}. Skipping", ref);
            acknowledgement.acknowledge();
            return;
        }

        transaction.setStatus(TransactionStatus.REVERSED);
        transactionService.updateTransaction(transaction);
        acknowledgement.acknowledge();
        log.trace("Compensation completed. Reference number: {}", ref);
    }


    @Transactional
    @KafkaListener(
            topics = KafkaTopics.COMPENSATE_FAILED,
            groupId = "transaction-group",
            containerFactory = "compensateRequestListenerFactory")
    public void onCompensationFailure(CompensateResultEvent event, Acknowledgment acknowledgement) {
        String ref = event.getReferenceNumber();
        log.trace("Compensation Failed. Reference number: {}", ref);
        Transaction transaction = transactionService.getTransactionByRefNo(ref);
        transaction.setCompensateRetryCount(transaction.getCompensateRetryCount() + 1);
        transactionService.updateTransaction(transaction);
        acknowledgement.acknowledge();
    }
}

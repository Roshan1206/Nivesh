package com.nivesh.transaction.service;

import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateResultEvent;
import com.nivesh.library.dto.event.TransferResultEvent;
import com.nivesh.transaction.entity.JournalEntry;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.DrCr;
import com.nivesh.transaction.entity.enums.OutboxStatus;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.exception.TransactionNotFoundException;
import com.nivesh.transaction.repository.OutboxEventRepository;
import com.nivesh.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class TransactionConsumer {

    private final JournalEntryService journalEntryService;

    private final OutboxEventService outboxEventService;

    private final TransactionRepository transactionRepository;

    public TransactionConsumer(JournalEntryService journalEntryService, OutboxEventService outboxEventService,
                               TransactionRepository transactionRepository) {
        this.journalEntryService = journalEntryService;
        this.outboxEventService = outboxEventService;
        this.transactionRepository = transactionRepository;
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
            return;
        }
        try {
            journalEntryService.writeLedger(event);
            outboxEventService.save(event.getTransferRequest().getReferenceNumber(), OutboxStatus.PUBLISHED);
            log.trace("Transfer successful. Reference number: {}", ref);
        } catch (Exception e) {
            log.error("Error processing writing ledger entry for transfer request");
            acknowledgement.acknowledge();
            return;
        }
        acknowledgement.acknowledge();
        log.trace("Transfer completed. Reference number: {}", ref);
    }

    @Transactional
    @KafkaListener(
            topics = KafkaTopics.COMPENSATE_SUCCESS,
            groupId = "transaction-group",
            containerFactory = "compensateRequestListenerFactory")
    public void onCompensationSuccess(CompensateResultEvent event, Acknowledgment acknowledgement) {
        String ref = event.getReferenceNumber();
        log.trace("Compensation successful. Reference number: {}", ref);
        Transaction transaction = transactionRepository.findByReferenceNumber(ref).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found. Reference number: " + ref));

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            log.trace("Duplicate compensation. Reference number: {}. Skipping", ref);
            acknowledgement.acknowledge();
            return;
        }

        transaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(transaction);
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
        Transaction transaction = transactionRepository.findByReferenceNumber(ref).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found. Reference number: " + ref));

        transaction.setCompensateRetryCount(transaction.getCompensateRetryCount() + 1);
        transactionRepository.save(transaction);
        acknowledgement.acknowledge();
    }
}

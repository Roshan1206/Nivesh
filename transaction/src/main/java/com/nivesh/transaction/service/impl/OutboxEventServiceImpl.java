package com.nivesh.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.event.CreditRequestedEvent;
import com.nivesh.library.dto.event.DebitRequestedEvent;
import com.nivesh.library.dto.event.TransferRequestedEvent;
import com.nivesh.transaction.entity.OutboxEvent;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.OutboxStatus;
import com.nivesh.transaction.exception.OutboxEventNotFoundException;
import com.nivesh.transaction.exception.OutboxSerialiizationException;
import com.nivesh.transaction.repository.OutboxEventRepository;
import com.nivesh.transaction.service.OutboxEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OutboxEventServiceImpl implements OutboxEventService {

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventServiceImpl(ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository) {
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public void publishTransferRequested(Transaction transaction) {
        TransferRequestedEvent event = TransferRequestedEvent.builder()
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().toString())
                .build();

        save(transaction.getReferenceNumber(), KafkaTopics.TRANSFER_REQUESTED, "TRANSFER_REQUESTED", event);
        log.debug("Outbox: TRANSFER_REQUESTED queued. ref={}", transaction.getReferenceNumber());
    }

    @Override
    public void publishDebitRequested(Transaction transaction) {
        DebitRequestedEvent event = DebitRequestedEvent.builder()
                .accountId(transaction.getSourceAccountId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().toString())
                .build();

        save(transaction.getReferenceNumber(), KafkaTopics.DEBIT_REQUESTED, "DEBIT_REQUEST", event);
        log.debug("Outbox: DEBIT_REQUESTED queued. ref={}", transaction.getReferenceNumber());
    }

    @Override
    public void publishCreditRequested(Transaction transaction) {
        CreditRequestedEvent event = CreditRequestedEvent.builder()
                .sourceAccountId(transaction.getSourceAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .idempotencyKey(transaction.getIdempotencyKey())
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().toString())
                .build();

        log.debug("Outbox: CREDIT_REQUESTED queued. ref={}", transaction.getReferenceNumber());
    }

    @Override
    public void publishCompensateRequested(Transaction transaction) {
        CompensateRequestEvent event = CompensateRequestEvent.builder()
                .sourceAccountId(transaction.getSourceAccountId())
                .idempotencyKey(transaction.getIdempotencyKey() + "-comp")
                .referenceNumber(transaction.getReferenceNumber())
                .amount(transaction.getAmount())
                .build();

        save(transaction.getReferenceNumber(), KafkaTopics.COMPENSATE_REQUEST, "COMPENSATE_REQUEST", event);
        log.debug("Outbox: COMPENSATE_REQUEST queued. ref={}", transaction.getReferenceNumber());
    }

    @Override
    public void save(String aggregateId, OutboxStatus status) {
        OutboxEvent event = getOutboxEvent(aggregateId);
        event.setOutboxStatus(status);
        outboxEventRepository.save(event);
    }

    private void save(String referenceNumber, String topic, String eventType, Object payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(referenceNumber)
                .topic(topic)
                .eventType(eventType)
                .payload(serialize(payload))
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new OutboxSerialiizationException("Failed to serialize outbox event: " + event.getClass().getSimpleName());
        }
    }

    private OutboxEvent getOutboxEvent(String aggregateId) {
        return outboxEventRepository.findByAggregateId(aggregateId).orElseThrow(
                () -> new OutboxEventNotFoundException("Event not found")
        );
    }

}

package com.nivesh.account.service;

import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.event.CompensateResultEvent;
import com.nivesh.library.dto.event.TransferRequestedEvent;
import com.nivesh.library.dto.event.TransferResultEvent;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.response.AccountTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Communication class using kafka for consuming and producing events related to account balance.
 */
@Slf4j
@Component
public class AccountConsumer {

    /** Responsible for managing accounts */
    private final AccountService accountService;

    /** Emits events */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Injecting required dependency using constructor injection
     */
    public AccountConsumer(AccountService accountService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * Transfer funds from one account to another. Sends result using kafka.
     */
    @Transactional
    @KafkaListener(
            topics = KafkaTopics.TRANSFER_REQUESTED,
            groupId = "transaction-group",
            containerFactory = "transferRequestListenerFactory"
    )
    public void onTransferRequest(TransferRequestedEvent event, Acknowledgment ack) {
        String ref = event.getReferenceNumber();
        log.trace("Transfer started in Account. Reference number: {}", ref);
        AccountTransactionResponse debit = accountService.debit(event.getSourceAccountId(),
                "debit-" + event.getIdempotencyKey(), new AmountTransactionRequest(event.getAmount()));
        TransferResultEvent resultEvent = new TransferResultEvent();
        resultEvent.setTransferRequest(event);
        if (debit.getStatus() != 200) {
            log.error("Debit failed for transfer request.");
            resultEvent.setSuccess(false);
            resultEvent.setFailureReason("Debit failed for transfer request.");
            kafkaTemplate.send(KafkaTopics.TRANSFER_RESULT, ref, resultEvent);
            ack.acknowledge();
            return;
        }
        resultEvent.setPostDebitBalance(debit.getRunningBalance());
        AccountTransactionResponse credit = accountService.credit(event.getDestinationAccountId(),
                "credit-" + event.getIdempotencyKey(), new AmountTransactionRequest(event.getAmount()));
        resultEvent.setPostCreditBalance(credit.getRunningBalance());
        resultEvent.setSuccess(true);
        ack.acknowledge();

        log.trace("Transfer Succeed in Account. Reference number: {}", ref);
        kafkaTemplate.send(KafkaTopics.TRANSFER_RESULT, ref, resultEvent);
    }


    /**
     * Compensate funds for failed transactions. Sends result using kafka.
     */
    @Transactional
    @KafkaListener(
            topics = KafkaTopics.COMPENSATE_REQUEST,
            groupId = "transaction-group",
            containerFactory = "compensateRequestListenerFactory"
    )
    public void onCompensateRequest(CompensateRequestEvent event, Acknowledgment ack) {
        String ref = event.getReferenceNumber();
        log.trace("Compensation started in Account. Reference number: {}", ref);
        AccountTransactionResponse response = accountService.getTransactionResponse("credit-" + event.getIdempotencyKey());
        if (response != null){
            log.trace("Credit has already been processed. Skipping Compensation");
        }
        String idempotencyKey = "comp-" + event.getIdempotencyKey();
        AmountTransactionRequest request = new AmountTransactionRequest(event.getAmount());
        AccountTransactionResponse transactionResponse = accountService.credit(event.getSourceAccountId(), idempotencyKey, request);
        CompensateResultEvent resultEvent = new CompensateResultEvent();
        if (transactionResponse.getStatus() != 200) {
            log.trace("Compensation failed in Account. Reference number: {}", ref);
            kafkaTemplate.send(KafkaTopics.COMPENSATE_FAILED, ref, resultEvent);
            return;
        }
        log.trace("Compensation Succeed in Account. Reference number: {}", ref);
        kafkaTemplate.send(KafkaTopics.COMPENSATE_SUCCESS, ref, resultEvent);
    }
}

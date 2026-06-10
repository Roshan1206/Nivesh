package com.nivesh.account.service;

import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.event.CompensateResultEvent;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.response.AccountTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AccountConsumer {

    private final AccountService accountService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountConsumer(AccountService accountService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(
            topics = KafkaTopics.COMPENSATE_REQUEST,
            groupId = "transaction-group",
            containerFactory = "compensateResultListenerFactory"
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
        CompensateResultEvent resultEvent = new CompensateResultEvent(ref);
        if (transactionResponse.getStatus() != 200) {
            log.trace("Compensation failed in Account. Reference number: {}", ref);
            kafkaTemplate.send(KafkaTopics.COMPENSATE_FAILED, ref, resultEvent);
            return;
        }
        log.trace("Compensation Succeed in Account. Reference number: {}", ref);
        kafkaTemplate.send(KafkaTopics.COMPENSATE_SUCCESS, ref, resultEvent);
    }
}

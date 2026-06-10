package com.nivesh.transaction.service;

import com.nivesh.library.constant.KafkaTopics;
import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.event.CreditFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionProducer (KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

//    public void publishCreditFailed(CreditFailedEvent event) {
//        kafkaTemplate.send(KafkaTopics.CREDIT_FAILED, event.getReferenceNumber(), event);
//        log.debug("Published CreditFailed. ref= {}, reason= {}", event.getReferenceNumber(), event.getFailureReason());
//    }

    public void publishCompensateRequest(CompensateRequestEvent event) {
        kafkaTemplate.send(KafkaTopics.COMPENSATE_REQUEST, event.getReferenceNumber(), event);
        log.debug("Compensation started. Ref = {}", event.getReferenceNumber());
    }

    public void publishDeadLetter(String referenceNumber, String reason) {
        kafkaTemplate.send(KafkaTopics.DEAD_LETTER, referenceNumber, referenceNumber);
        log.error("Published DeadLetter. Ref no= {}, reason = {}", referenceNumber, reason);
    }
}

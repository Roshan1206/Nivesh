package com.nivesh.library.configuration.kafka;

import com.nivesh.library.dto.event.CompensateRequestEvent;
import com.nivesh.library.dto.event.CompensateResultEvent;
import com.nivesh.library.dto.event.CreditRequestedEvent;
import com.nivesh.library.dto.event.CreditResultEvent;
import com.nivesh.library.dto.event.DebitRequestedEvent;
import com.nivesh.library.dto.event.DebitResultEvent;
import com.nivesh.library.dto.event.TransferRequestedEvent;
import com.nivesh.library.dto.event.TransferResultEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@AutoConfiguration
@ConditionalOnProperty(prefix = "nivesh.kafka", name = "enabled", havingValue = "true")
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreditRequestedEvent> creditRequestListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, CreditRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(CreditRequestedEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreditResultEvent> creditResultListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, CreditResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(CreditResultEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DebitRequestedEvent> debitRequestListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, DebitRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(DebitRequestedEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DebitResultEvent> debitResultListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, DebitResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(DebitResultEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompensateRequestEvent> compensateRequestListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, CompensateRequestEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(CompensateRequestEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompensateResultEvent> compensateResultListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, CompensateResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(CompensateResultEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferRequestedEvent> transferRequestListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, TransferRequestedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(TransferRequestedEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferResultEvent> transferResultListenerFactory()
    {
        ConcurrentKafkaListenerContainerFactory<String, TransferResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(TransferResultEvent.class));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> eventClass) {
        return consumerFactory(eventClass, false);
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> eventClass, boolean header) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties();
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(eventClass, header);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }
}

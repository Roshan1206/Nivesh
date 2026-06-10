package com.nivesh.library.configuration.kafka;

import com.nivesh.library.dto.event.CompensateRequestEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@AutoConfiguration
@ConditionalOnProperty(prefix = "nivesh.kafka", name = "enabled", havingValue = "true")
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, CompensateRequestEvent> compensateRequestConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties();
        JsonDeserializer<CompensateRequestEvent> deserializer =
                new JsonDeserializer<>(CompensateRequestEvent.class, false);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompensateRequestEvent> compensateResultListenerFactory(
            ConsumerFactory<String, CompensateRequestEvent> compensateRequestConsumerFactory
    )
    {
        ConcurrentKafkaListenerContainerFactory<String, CompensateRequestEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(compensateRequestConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}

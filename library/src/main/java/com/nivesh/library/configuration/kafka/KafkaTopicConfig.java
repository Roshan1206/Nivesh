package com.nivesh.library.configuration.kafka;

import com.nivesh.library.constant.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@AutoConfiguration
@ConditionalOnProperty(prefix = "nivesh.kafka", name = "enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic compensateRequestTopic() {
        return TopicBuilder.name(KafkaTopics.COMPENSATE_REQUEST)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(KafkaTopics.DEAD_LETTER)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic compensateSuccess() {
        return TopicBuilder.name(KafkaTopics.COMPENSATE_SUCCESS)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic compensateFailed() {
        return TopicBuilder.name(KafkaTopics.COMPENSATE_FAILED)
                .partitions(3).replicas(1).build();
    }
}

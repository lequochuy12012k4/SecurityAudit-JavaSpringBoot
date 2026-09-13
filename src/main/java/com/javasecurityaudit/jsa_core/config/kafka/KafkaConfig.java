package com.javasecurityaudit.jsa_core.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${app.kafka.topic:document-sync-events}")
    private String topic;

    @Value("${app.kafka.partitions:1}")
    private int partitions;

    @Value("${app.kafka.replication-factor}")
    private int replicationFactor;

    @Bean
    public NewTopic documentSyncEventsTopic() {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}

package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaDocumentSyncProducer {

    private final KafkaTemplate<String, DocumentSyncEvent> kafkaTemplate;

    @Value("${app.kafka.topic:document-sync-events}")
    private String topic;

    public void send(DocumentSyncEvent event) {
        try {
            kafkaTemplate.send(topic, event.getEntityType(), event);
            log.info("Published document sync event to Kafka topic {}: {}", topic, event);
        } catch (Exception e) {
            log.error("Failed to publish document sync event to Kafka: {}", e.getMessage(), e);
        }
    }
}

package com.javasecurityaudit.jsa_core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javasecurityaudit.jsa_core.document.InvoiceDocument;
import com.javasecurityaudit.jsa_core.document.UserDocument;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncAction;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncEntityType;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncEvent;
import com.javasecurityaudit.jsa_core.repository.elasticsearch.InvoiceElasticsearchRepository;
import com.javasecurityaudit.jsa_core.repository.elasticsearch.UserElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaElasticsearchSyncConsumer {

    private final UserElasticsearchRepository userElasticsearchRepository;
    private final InvoiceElasticsearchRepository invoiceElasticsearchRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic:document-sync-events}", groupId = "${spring.kafka.consumer.group-id:jsa-doc-sync-group}")
    public void consume(DocumentSyncEvent event) {
        if (event == null || event.getEntityType() == null || event.getAction() == null) {
            log.warn("Received empty document sync event from Kafka");
            return;
        }

        try {
            DocumentSyncEntityType entityType = DocumentSyncEntityType.valueOf(event.getEntityType());
            DocumentSyncAction action = DocumentSyncAction.valueOf(event.getAction());

            if (action == DocumentSyncAction.DELETE) {
                switch (entityType) {
                    case USER -> userElasticsearchRepository.deleteById(event.getPayload());
                    case INVOICE -> invoiceElasticsearchRepository.deleteById(event.getPayload());
                }
                log.info("Deleted Elasticsearch document via Kafka: entityType={}, payload={}", entityType, event.getPayload());
                return;
            }

            if (action == DocumentSyncAction.SAVE) {
                switch (entityType) {
                    case USER -> {
                        UserDocument userDocument = objectMapper.readValue(event.getPayload(), UserDocument.class);
                        userElasticsearchRepository.save(userDocument);
                    }
                    case INVOICE -> {
                        InvoiceDocument invoiceDocument = objectMapper.readValue(event.getPayload(), InvoiceDocument.class);
                        invoiceElasticsearchRepository.save(invoiceDocument);
                    }
                }
                log.info("Synced Elasticsearch document via Kafka: entityType={}, action={}", entityType, action);
            }
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            log.error("Error handling document sync event from Kafka: {}", event, ex);
        }
    }
}

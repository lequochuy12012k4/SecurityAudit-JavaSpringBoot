package com.javasecurityaudit.jsa_core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javasecurityaudit.jsa_core.document.InvoiceDocument;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncAction;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncEntityType;
import com.javasecurityaudit.jsa_core.dto.event.DocumentSyncEvent;
import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.dto.response.PageResponse;
import com.javasecurityaudit.jsa_core.entity.Invoice;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.mapper.InvoiceMapper;
import com.javasecurityaudit.jsa_core.repository.JPA.InvoiceRepository;
import com.javasecurityaudit.jsa_core.repository.elasticsearch.InvoiceElasticsearchRepository;
import com.javasecurityaudit.jsa_core.service.InvoiceService;
import com.javasecurityaudit.jsa_core.service.KafkaDocumentSyncProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    InvoiceElasticsearchRepository invoiceElasticsearchRepository;
    StringRedisTemplate redisTemplate;
    InvoiceMapper invoiceMapper;
    KafkaDocumentSyncProducer kafkaDocumentSyncProducer;
    ObjectMapper objectMapper;

    private static final String INVOICE_CREATE_LOCK_PREFIX = "invoice:create:lock:";
    private static final String INVOICE_CREATE_ATTEMPT_PREFIX = "invoice:create:attempts:";
    private static final String INVOICE_CREATE_BLOCK_PREFIX = "invoice:create:block:";
    private static final long CREATE_LOCK_TTL_SECONDS = 5;
    private static final long CREATE_RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final long CREATE_RATE_LIMIT_BLOCK_SECONDS = 60;
    private static final long CREATE_RATE_LIMIT_MAX_ATTEMPTS = 5;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        String normalizedCode = request.getInvoiceCode() != null ? request.getInvoiceCode().trim() : "";
        if (normalizedCode.isBlank()) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
        }

        String blockKey = buildBlockKey(normalizedCode);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new AppException(ErrorCode.INVOICE_CREATION_RATE_LIMITED);
        }

        String attemptKey = buildAttemptKey(normalizedCode);
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, CREATE_RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (attempts != null && attempts > CREATE_RATE_LIMIT_MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(blockKey, "1", CREATE_RATE_LIMIT_BLOCK_SECONDS, TimeUnit.SECONDS);
            redisTemplate.delete(attemptKey);
            throw new AppException(ErrorCode.INVOICE_CREATION_RATE_LIMITED);
        }

        String lockKey = buildLockKey(normalizedCode);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", CREATE_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new AppException(ErrorCode.INVOICE_CREATION_RATE_LIMITED);
        }

        try {
            if (invoiceRepository.existsByInvoiceCode(normalizedCode)) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
            }

            Invoice invoice = invoiceMapper.toInvoice(request);
            invoice.setInvoiceCode(normalizedCode);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setDueDate(LocalDateTime.now().plusDays(7));
            invoice.setStatus("CREATED");

            Invoice saved = invoiceRepository.save(invoice);
            redisTemplate.delete(attemptKey);
            publishInvoiceEvent(saved, DocumentSyncAction.SAVE);
            return invoiceMapper.toInvoiceResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(String invoiceId, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));

        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerEmail(request.getCustomerEmail());
        invoice.setCustomerPhone(request.getCustomerPhone());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setDescription(request.getDescription());

        Invoice updated = invoiceRepository.save(invoice);
        publishInvoiceEvent(updated, DocumentSyncAction.SAVE);
        return invoiceMapper.toInvoiceResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        invoiceRepository.delete(invoice);
        publishInvoiceEvent(invoice, DocumentSyncAction.DELETE);
    }

    private String buildLockKey(String invoiceCode) {
        return INVOICE_CREATE_LOCK_PREFIX + invoiceCode;
    }

    private String buildAttemptKey(String invoiceCode) {
        return INVOICE_CREATE_ATTEMPT_PREFIX + invoiceCode;
    }

    private String buildBlockKey(String invoiceCode) {
        return INVOICE_CREATE_BLOCK_PREFIX + invoiceCode;
    }

    private void publishInvoiceEvent(Invoice invoice, DocumentSyncAction action) {
        try {
            if (action == DocumentSyncAction.DELETE) {
                kafkaDocumentSyncProducer.send(DocumentSyncEvent.builder()
                        .entityType(DocumentSyncEntityType.INVOICE.name())
                        .action(DocumentSyncAction.DELETE.name())
                        .payload(invoice.getId())
                        .build());
                return;
            }

            InvoiceDocument invoiceDocument = invoiceMapper.toInvoiceDocument(invoice);
            kafkaDocumentSyncProducer.send(DocumentSyncEvent.builder()
                    .entityType(DocumentSyncEntityType.INVOICE.name())
                    .action(DocumentSyncAction.SAVE.name())
                    .payload(objectMapper.writeValueAsString(invoiceDocument))
                    .build());
        } catch (JsonProcessingException e) {
            log.error("Lỗi serialize InvoiceDocument sang Kafka event: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
        public PageResponse<InvoiceResponse> search(String keyword, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank()) {
            return PageResponse.<InvoiceResponse>builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .build();
        }
        Page<InvoiceDocument> searchPage = invoiceElasticsearchRepository.search(normalizedKeyword, PageRequest.of(page, size));
        List<String> ids = searchPage.getContent().stream().map(InvoiceDocument::getId).toList();
        Map<String, Invoice> invoicesById = invoiceRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Invoice::getId, Function.identity()));
        List<InvoiceResponse> content = ids.stream().map(invoicesById::get).filter(invoice -> invoice != null)
            .map(invoiceMapper::toInvoiceResponse).toList();
        return PageResponse.<InvoiceResponse>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(searchPage.getTotalElements())
            .totalPages(searchPage.getTotalPages())
            .build();
    }

}

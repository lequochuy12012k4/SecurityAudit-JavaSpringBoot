package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.document.InvoiceDocument;
import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.entity.Invoice;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.mapper.InvoiceMapper;
import com.javasecurityaudit.jsa_core.repository.JPA.InvoiceRepository;
import com.javasecurityaudit.jsa_core.repository.elasticsearch.InvoiceElasticsearchRepository;
import com.javasecurityaudit.jsa_core.service.InvoiceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
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
            try {
                InvoiceDocument invoiceDocument = invoiceMapper.toInvoiceDocument(saved);
                invoiceElasticsearchRepository.save(invoiceDocument);
            } catch (Exception e) {
                log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
            }
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
        try {
            InvoiceDocument invoiceDocument = invoiceMapper.toInvoiceDocument(updated);
            invoiceElasticsearchRepository.save(invoiceDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
        return invoiceMapper.toInvoiceResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        invoiceRepository.delete(invoice);
        try {
            InvoiceDocument invoiceDocument = invoiceMapper.toInvoiceDocument(invoice);
            invoiceElasticsearchRepository.delete(invoiceDocument);
        } catch (Exception e) {
            log.error("Lỗi đồng bộ Elasticsearch: {}", e.getMessage());
        }
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

}

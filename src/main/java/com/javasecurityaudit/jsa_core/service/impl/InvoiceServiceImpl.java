package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.entity.Invoice;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.mapper.InvoiceMapper;
import com.javasecurityaudit.jsa_core.repository.InvoiceRepository;
import com.javasecurityaudit.jsa_core.service.InvoiceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    StringRedisTemplate redisTemplate;
    InvoiceMapper invoiceMapper;

    private static final String INVOICE_CREATE_LOCK_PREFIX = "invoice:create:lock:";
    private static final long CREATE_LOCK_TTL_SECONDS = 5;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        String normalizedCode = request.getInvoiceCode() != null ? request.getInvoiceCode().trim() : "";
        String lockKey = buildLockKey(normalizedCode);

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", CREATE_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new AppException(ErrorCode.INVOICE_CREATION_RATE_LIMITED);
        }

        try {
            if (normalizedCode.isBlank()) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
            }

            if (invoiceRepository.existsByInvoiceCode(normalizedCode)) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
            }

            Invoice invoice = invoiceMapper.toInvoice(request);
            invoice.setInvoiceCode(normalizedCode);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setDueDate(LocalDateTime.now().plusDays(7));
            invoice.setStatus("CREATED");

            Invoice saved = invoiceRepository.save(invoice);
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
        return invoiceMapper.toInvoiceResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));
        invoiceRepository.delete(invoice);
    }

    private String buildLockKey(String invoiceCode) {
        return INVOICE_CREATE_LOCK_PREFIX + invoiceCode;
    }

}

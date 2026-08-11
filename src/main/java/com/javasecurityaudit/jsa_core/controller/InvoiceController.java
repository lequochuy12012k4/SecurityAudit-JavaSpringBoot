package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final MessageSource messageSource;

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.INVOICE_CREATE, description = "Tạo hóa đơn mới")
    public BaseResponse<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return BaseResponse.success(getMessage("success.invoice.created"), invoiceService.createInvoice(request));
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.INVOICE_GET, description = "Xem thông tin hóa đơn")
    public BaseResponse<InvoiceResponse> getInvoice(@PathVariable String invoiceId) {
        return BaseResponse.success(invoiceService.getInvoice(invoiceId));
    }

    @PutMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.INVOICE_UPDATE, description = "Cập nhật hóa đơn")
    public BaseResponse<InvoiceResponse> updateInvoice(
            @PathVariable String invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return BaseResponse.success(getMessage("success.invoice.updated"), invoiceService.updateInvoice(invoiceId, request));
    }

    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @LogActivity(action = AuditAction.INVOICE_DELETE, description = "Xóa hóa đơn")
    public BaseResponse<String> deleteInvoice(@PathVariable String invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return BaseResponse.success(getMessage("success.invoice.deleted"));
    }
}

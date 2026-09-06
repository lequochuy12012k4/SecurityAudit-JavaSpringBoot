package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.dto.response.PageResponse;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoice(String invoiceId);
    InvoiceResponse updateInvoice(String invoiceId, UpdateInvoiceRequest request);
    void deleteInvoice(String invoiceId);
    PageResponse<InvoiceResponse> search(String keyword, int page, int size);
}

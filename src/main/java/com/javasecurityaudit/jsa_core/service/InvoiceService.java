package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.request.UpdateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoice(String invoiceId);
    InvoiceResponse updateInvoice(String invoiceId, UpdateInvoiceRequest request);
    void deleteInvoice(String invoiceId);
}

package com.javasecurityaudit.jsa_core.mapper;

import com.javasecurityaudit.jsa_core.dto.request.CreateInvoiceRequest;
import com.javasecurityaudit.jsa_core.dto.response.InvoiceResponse;
import com.javasecurityaudit.jsa_core.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "invoiceDate", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Invoice toInvoice(CreateInvoiceRequest request);

    InvoiceResponse toInvoiceResponse(Invoice invoice);
}

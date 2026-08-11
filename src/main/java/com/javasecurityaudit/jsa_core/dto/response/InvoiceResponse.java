package com.javasecurityaudit.jsa_core.dto.response;

import com.javasecurityaudit.jsa_core.dto.response.audit.AuditResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse extends AuditResponse {

    String id;
    String invoiceCode;
    String customerName;
    String customerEmail;
    String customerPhone;
    BigDecimal totalAmount;
    String status;
    String description;
    LocalDateTime invoiceDate;
    LocalDateTime dueDate;
}

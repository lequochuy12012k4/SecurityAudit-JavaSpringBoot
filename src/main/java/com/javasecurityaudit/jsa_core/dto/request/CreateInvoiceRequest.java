package com.javasecurityaudit.jsa_core.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateInvoiceRequest {

    @NotBlank(message = "validation.invoice.code.required")
    @Size(max = 50, message = "validation.invoice.code.size")
    String invoiceCode;

    @NotBlank(message = "validation.invoice.customer.name.required")
    @Size(max = 200, message = "validation.invoice.customer.name.size")
    String customerName;

    @Size(max = 200, message = "validation.invoice.customer.email.size")
    String customerEmail;

    @Size(max = 20, message = "validation.invoice.customer.phone.size")
    String customerPhone;

    @NotNull(message = "validation.invoice.total.amount.required")
    @DecimalMin(value = "0.0", inclusive = false, message = "validation.invoice.total.amount.min")
    BigDecimal totalAmount;

    String description;
}
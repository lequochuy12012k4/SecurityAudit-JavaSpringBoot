package com.javasecurityaudit.jsa_core.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateInvoiceRequest {

    @NotBlank(message = "validation.invoice.customer.name.required")
    @Size(max = 200, message = "validation.invoice.customer.name.size")
    String customerName;

    @Size(max = 200, message = "validation.invoice.customer.email.size")
    String customerEmail;

    @Size(max = 20, message = "validation.invoice.customer.phone.size")
    String customerPhone;

    @DecimalMin(value = "0.0", inclusive = false, message = "validation.invoice.total.amount.min")
    BigDecimal totalAmount;

    String description;
}
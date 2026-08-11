package com.javasecurityaudit.jsa_core.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.javasecurityaudit.jsa_core.base.audit.BaseAuditTrail;

@Entity
@Table(name = "invoices", uniqueConstraints = {
    @UniqueConstraint(name = "uk_invoice_code", columnNames = "invoice_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice extends BaseAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    String invoiceCode;

    @Column(name = "customer_name", nullable = false, length = 200)
    String customerName;

    @Column(name = "customer_email", length = 200)
    String customerEmail;

    @Column(name = "customer_phone", length = 20)
    String customerPhone;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    BigDecimal totalAmount;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    String status = "DRAFT";

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "invoice_date", nullable = false)
    LocalDateTime invoiceDate;

    @Column(name = "due_date")
    LocalDateTime dueDate;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    Long version = 0L;
}
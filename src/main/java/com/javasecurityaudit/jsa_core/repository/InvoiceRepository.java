package com.javasecurityaudit.jsa_core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javasecurityaudit.jsa_core.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    boolean existsByInvoiceCode(String invoiceCode);
    Optional<Invoice> findByInvoiceCode(String invoiceCode);
}
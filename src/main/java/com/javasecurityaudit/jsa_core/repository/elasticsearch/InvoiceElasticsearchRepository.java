package com.javasecurityaudit.jsa_core.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.javasecurityaudit.jsa_core.document.InvoiceDocument;

public interface InvoiceElasticsearchRepository extends ElasticsearchRepository<InvoiceDocument,String>{

	@Query("{\"multi_match\":{\"query\":\"?0\",\"fields\":[\"invoiceCode^4\",\"customerName^3\",\"customerEmail^2\",\"customerPhone\",\"description\"],\"type\":\"best_fields\",\"fuzziness\":\"AUTO\"}}")
	Page<InvoiceDocument> search(String keyword, Pageable pageable);
}

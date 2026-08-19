package com.javasecurityaudit.jsa_core.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.javasecurityaudit.jsa_core.document.InvoiceDocument;

public interface InvoiceElasticsearchRepository extends ElasticsearchRepository<InvoiceDocument,String>{
    
}

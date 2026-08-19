package com.javasecurityaudit.jsa_core.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.javasecurityaudit.jsa_core.document.UserDocument;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserDocument,String> {
    
}

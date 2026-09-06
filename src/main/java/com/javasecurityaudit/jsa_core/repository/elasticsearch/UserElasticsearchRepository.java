package com.javasecurityaudit.jsa_core.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.javasecurityaudit.jsa_core.document.UserDocument;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserDocument,String> {

	@Query("{\"bool\":{\"should\":[{\"multi_match\":{\"query\":\"?0\",\"fields\":[\"username^3\",\"email^2\",\"fullName^3\"],\"type\":\"best_fields\",\"fuzziness\":\"AUTO\"}},{\"wildcard\":{\"username\":{\"value\":\"*?0*\",\"case_insensitive\":true}}},{\"wildcard\":{\"email\":{\"value\":\"*?0*\",\"case_insensitive\":true}}}],\"minimum_should_match\":1}}")
	Page<UserDocument> search(String keyword, Pageable pageable);
}

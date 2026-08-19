package com.javasecurityaudit.jsa_core.config.elasticsearch;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

@Configuration
@EnableElasticsearchRepositories(basePackages="com.javasecurityaudit.jsa_core.repository.elasticsearch")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                // 1. Địa chỉ Elasticsearch server
                .connectedTo("localhost:9200")
                .withHeaders(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Accept", "application/vnd.elasticsearch+json; compatible-with=8");
                    headers.add("Content-Type", "application/vnd.elasticsearch+json; compatible-with=8");
                    return headers;
                })
                
                // 2. Thời gian Timeout
                // .withConnectTimeout(Duration.ofSeconds(5))
                // .withSocketTimeout(Duration.ofSeconds(30))
                
                // 3. Nếu có Username/Password thì mở comment dòng dưới:
                // .withBasicAuth("elastic", "your_password")
                
                // 4. Nếu dùng HTTPS tự ký (Self-signed certificate) trong môi trường Dev:
                // .usingSsl()
                
                .build();
    }
}

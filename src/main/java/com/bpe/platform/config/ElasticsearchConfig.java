package com.bpe.platform.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {
    
    @Value("${app.elasticsearch.host}")
    private String elasticsearchHost;
    
    @Value("${app.elasticsearch.username}")
    private String username;
    
    @Value("${app.elasticsearch.password}")
    private String password;
    
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        URI uri = URI.create(elasticsearchHost);
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        
        // 인증 정보 설정
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );
        
        // RestClient 생성
        RestClient restClient = RestClient.builder(host)
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            )
            .build();
        
        // Transport 생성
        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );
        
        // ElasticsearchClient 생성
        return new ElasticsearchClient(transport);
    }
}


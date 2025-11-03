package com.bpe.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 서비스 (Qwen2.5 모델 호출)
 */
@Service
public class LlmService {
    
    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.llm.server.url}")
    private String llmServerUrl;
    
    @Value("${app.llm.server.enabled:true}")
    private boolean llmEnabled;
    
    @Value("${app.llm.server.timeout:30000}")
    private int timeout;
    
    @Value("${app.llm.server.max-tokens:300}")
    private int maxTokens;
    
    @Value("${app.llm.server.temperature:0.2}")
    private double temperature;
    
    public LlmService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * LLM에게 프롬프트 전송하고 답변 받기
     * 
     * @param prompt 프롬프트
     * @return LLM 응답 텍스트
     */
    public String generateResponse(String prompt) {
        if (!llmEnabled) {
            logger.debug("LLM 서비스가 비활성화되어 있습니다.");
            return null;
        }
        
        try {
            String url = llmServerUrl + "/completion";
            
            // 요청 본문 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("n_predict", maxTokens);
            requestBody.put("temperature", temperature);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            logger.debug("LLM 서버에 요청 전송: url={}, prompt length={}", url, prompt.length());
            
            // API 호출
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // JSON 응답 파싱
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String content = jsonNode.path("content").asText("");
                
                logger.debug("LLM 응답 수신: content length={}", content.length());
                return content.trim();
            } else {
                logger.warn("LLM 서버 응답 오류: status={}", response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            logger.error("LLM 서버 연결 오류: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("LLM 응답 처리 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * LLM 서버 상태 확인
     * 
     * @return 서버가 정상이면 true
     */
    public boolean checkHealth() {
        if (!llmEnabled) {
            return false;
        }
        
        try {
            String url = llmServerUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.path("status").asText("");
                return "ok".equalsIgnoreCase(status);
            }
            return false;
        } catch (Exception e) {
            logger.debug("LLM 헬스 체크 실패: {}", e.getMessage());
            return false;
        }
    }
}


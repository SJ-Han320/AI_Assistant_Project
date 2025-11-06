package com.bpe.platform.controller;

import com.bpe.platform.service.DataChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 데이터 챗봇 컨트롤러
 * 모든 사용자 접근 가능
 */
@RestController
@RequestMapping("/api/data-chatbot")
public class DataChatbotController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataChatbotController.class);
    
    @Autowired
    private DataChatbotService dataChatbotService;
    
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        
        logger.info("데이터 챗봇 질문 수신: {}", question);
        
        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "질문을 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        DataChatbotService.DataChatbotResponse chatbotResponse = 
            dataChatbotService.answerQuestion(question);
        
        logger.info("데이터 챗봇 응답 생성: found={}, confidence={}, sources={}", 
                   chatbotResponse.isFound(), 
                   chatbotResponse.getConfidence(),
                   chatbotResponse.getSources().size());
        
        // sources를 Map 리스트로 변환하여 JSON 직렬화 보장
        List<Map<String, Object>> sourcesList = new java.util.ArrayList<>();
        for (DataChatbotService.SocialDataDocument source : chatbotResponse.getSources()) {
            Map<String, Object> sourceMap = new HashMap<>();
            sourceMap.put("id", source.getId());
            sourceMap.put("index", source.getIndex());
            sourceMap.put("title", source.getTitle());
            sourceMap.put("content", source.getContent());
            
            String url = source.getUrl();
            sourceMap.put("url", url);
            
            // URL 디버깅 로그
            if (url != null) {
                logger.debug("Source URL: {}", url);
            } else {
                logger.warn("Source URL is null for document: id={}, title={}", 
                           source.getId(), source.getTitle());
            }
            
            sourceMap.put("writerNick", source.getWriterNick());
            sourceMap.put("writerId", source.getWriterId());
            sourceMap.put("siteName", source.getSiteName());
            sourceMap.put("boardName", source.getBoardName());
            sourceMap.put("writeDate", source.getWriteDate());
            sourceMap.put("date", source.getDate());
            sourceMap.put("score", source.getScore());
            sourcesList.add(sourceMap);
        }
        
        logger.debug("변환된 sources 개수: {}, 첫 번째 source URL: {}", 
                     sourcesList.size(),
                     sourcesList.isEmpty() ? "없음" : sourcesList.get(0).get("url"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("answer", chatbotResponse.getAnswer());
        response.put("confidence", chatbotResponse.getConfidence());
        response.put("found", chatbotResponse.isFound());
        response.put("sources", sourcesList);
        
        return ResponseEntity.ok(response);
    }
}


package com.bpe.platform.controller;

import com.bpe.platform.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    
    @Autowired
    private ChatbotService chatbotService;
    
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        
        logger.info("챗봇 질문 수신: {}", question);
        
        if (question == null || question.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "질문을 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        ChatbotService.ChatbotResponse chatbotResponse = chatbotService.searchAnswer(question);
        
        logger.info("챗봇 응답 생성: found={}, confidence={}", chatbotResponse.isFound(), chatbotResponse.getConfidence());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("answer", chatbotResponse.getAnswer());
        response.put("confidence", chatbotResponse.getConfidence());
        response.put("found", chatbotResponse.isFound());
        
        if (chatbotResponse.getAlternativeAnswer() != null) {
            response.put("alternativeAnswer", chatbotResponse.getAlternativeAnswer());
        }
        
        return ResponseEntity.ok(response);
    }
}


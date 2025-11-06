package com.bpe.platform.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;
    
    @Autowired(required = false)
    private LlmService llmService;
    
    @Value("${app.elasticsearch.chatbot.index}")
    private String faqIndex;
    
    @Value("${app.llm.server.enabled:true}")
    private boolean llmEnabled;
    
    // RAG 사용 여부 설정 (ES 스코어가 낮을 때 LLM 사용)
    private static final double RAG_THRESHOLD = 0.4; // 스코어가 0.4 미만이면 LLM 사용
    
    /**
     * 사용자 질문에 대한 답변 검색
     * 하이브리드 방식: 텍스트 검색 + 키워드 매칭
     */
    public ChatbotResponse searchAnswer(String userQuestion) {
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return new ChatbotResponse(
                "질문을 입력해주세요.",
                null,
                0.0,
                false
            );
        }
        
        // ES 클라이언트가 없으면 기본 응답
        if (elasticsearchClient == null) {
            logger.warn("Elasticsearch 클라이언트가 null입니다. 챗봇 기능을 사용할 수 없습니다.");
            return new ChatbotResponse(
                "죄송합니다. 현재 챗봇 기능을 사용할 수 없습니다. Elasticsearch 클러스터에 연결할 수 없습니다.",
                null,
                0.0,
                false
            );
        }
        
        logger.debug("Elasticsearch 클라이언트 사용하여 검색 시작: question={}", userQuestion);
        
        try {
            // 1. 텍스트 검색 (BM25 스코어링)
            SearchResponse<FAQDocument> textSearchResponse = searchByText(userQuestion);
            
            // 2. 키워드 매칭 검색
            SearchResponse<FAQDocument> keywordSearchResponse = searchByKeywords(userQuestion);
            
            // 3. 결과 병합 및 스코어 조정
            List<FAQDocument> mergedResults = mergeSearchResults(
                textSearchResponse, 
                keywordSearchResponse
            );
            
            if (mergedResults.isEmpty()) {
                return new ChatbotResponse(
                    generateDefaultResponse(userQuestion),
                    null,
                    0.0,
                    false
                );
            }
            
            // 가장 높은 스코어의 결과 선택
            FAQDocument bestMatch = mergedResults.get(0);
            double bestScore = bestMatch.getScore();
            
            // RAG 전략: 스코어가 높으면 ES 답변 사용, 낮으면 LLM 사용
            if (bestScore >= RAG_THRESHOLD) {
                // ES 답변이 충분히 관련성이 높음 - 직접 반환
                logger.debug("ES 답변 사용 (스코어: {})", bestScore);
                return new ChatbotResponse(
                    bestMatch.getAnswer(),
                    null,
                    bestScore,
                    true
                );
            } else {
                // 스코어가 낮거나 LLM이 활성화되어 있으면 RAG 사용
                if (llmEnabled && llmService != null) {
                    logger.debug("RAG 모드: ES 검색 결과를 컨텍스트로 LLM 호출 (스코어: {})", bestScore);
                    return generateRagResponse(userQuestion, mergedResults);
                } else {
                    // LLM이 비활성화되어 있으면 기본 응답
                    logger.debug("LLM 비활성화: 기본 응답 반환");
                    return new ChatbotResponse(
                        generateDefaultResponse(userQuestion),
                        bestMatch.getScore() >= 0.2 ? bestMatch.getAnswer() : null,
                        bestScore,
                        false
                    );
                }
            }
            
        } catch (IOException e) {
            logger.error("Elasticsearch 검색 오류: {}", e.getMessage(), e);
            return new ChatbotResponse(
                "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                null,
                0.0,
                false
            );
        } catch (Exception e) {
            logger.error("챗봇 검색 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return new ChatbotResponse(
                "예기치 않은 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                null,
                0.0,
                false
            );
        }
    }
    
    /**
     * 텍스트 검색 (BM25)
     */
    private SearchResponse<FAQDocument> searchByText(String question) throws IOException {
        // 한국어 분석을 위한 쿼리
        Query textQuery = Query.of(q -> q
            .multiMatch(m -> m
                .query(question)
                .fields("question^3", "answer^2", "keywords^1.5")
                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                .fuzziness("AUTO")
            )
        );
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(faqIndex)
            .query(textQuery)
            .size(5)
        );
        
        return elasticsearchClient.search(searchRequest, FAQDocument.class);
    }
    
    /**
     * 키워드 매칭 검색
     */
    private SearchResponse<FAQDocument> searchByKeywords(String question) throws IOException {
        // 질문에서 키워드 추출 (간단한 방법)
        List<String> keywords = extractKeywords(question);
        
        if (keywords.isEmpty()) {
            return SearchResponse.of(s -> s
                .hits(h -> h.total(t -> t.value(0)))
            );
        }
        
        Query keywordQuery = Query.of(q -> q
            .bool(b -> b
                .should(keywords.stream()
                    .map(keyword -> Query.of(q2 -> q2
                        .match(m -> m
                            .field("keywords")
                            .query(keyword)
                            .boost(2.0f)
                        )
                    ))
                    .collect(Collectors.toList()))
                .minimumShouldMatch("1")
            )
        );
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(faqIndex)
            .query(keywordQuery)
            .size(5)
        );
        
        return elasticsearchClient.search(searchRequest, FAQDocument.class);
    }
    
    /**
     * 검색 결과 병합 및 스코어 조정
     */
    private List<FAQDocument> mergeSearchResults(
            SearchResponse<FAQDocument> textResults,
            SearchResponse<FAQDocument> keywordResults) {
        
        Map<String, FAQDocument> merged = new HashMap<>();
        
        // 텍스트 검색 결과 (가중치: 0.6)
        for (Hit<FAQDocument> hit : textResults.hits().hits()) {
            FAQDocument doc = hit.source();
            if (doc != null && hit.score() != null) {
                doc.setScore(hit.score() * 0.6);
                merged.put(doc.getId(), doc);
            }
        }
        
        // 키워드 검색 결과 (가중치: 0.4)
        for (Hit<FAQDocument> hit : keywordResults.hits().hits()) {
            FAQDocument doc = hit.source();
            if (doc != null && hit.score() != null) {
                String id = doc.getId();
                if (merged.containsKey(id)) {
                    // 이미 있는 경우 스코어 합산
                    merged.get(id).setScore(merged.get(id).getScore() + (hit.score() * 0.4));
                } else {
                    doc.setScore(hit.score() * 0.4);
                    merged.put(id, doc);
                }
            }
        }
        
        // 스코어 기준으로 정렬
        return merged.values().stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }
    
    /**
     * 질문에서 키워드 추출 (간단한 방법)
     */
    private List<String> extractKeywords(String question) {
        // 한국어 조사, 어미 제거를 위한 간단한 필터
        String[] stopWords = {"은", "는", "이", "가", "을", "를", "에", "에서", "의", "로", "으로", 
                             "와", "과", "도", "만", "부터", "까지", "께서", "한테", "에게", 
                             "에게서", "한테서", "처럼", "같이", "보다", "마다", "대로"};
        
        String normalized = question.toLowerCase().trim();
        for (String stopWord : stopWords) {
            normalized = normalized.replaceAll("\\s*" + stopWord + "\\s*", " ");
        }
        
        // 단어 단위로 분리
        List<String> keywords = new ArrayList<>();
        String[] words = normalized.split("\\s+");
        for (String word : words) {
            if (word.length() >= 2) { // 2글자 이상만 키워드로 인정
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * RAG 방식으로 답변 생성
     * ES 검색 결과를 컨텍스트로 LLM에 전달하여 자연스러운 답변 생성
     */
    private ChatbotResponse generateRagResponse(String userQuestion, List<FAQDocument> searchResults) {
        try {
            // 상위 3개 검색 결과를 컨텍스트로 사용
            int contextCount = Math.min(3, searchResults.size());
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("다음은 시스템 FAQ 문서들입니다:\n\n");
            
            for (int i = 0; i < contextCount; i++) {
                FAQDocument doc = searchResults.get(i);
                contextBuilder.append(String.format("[문서 %d]\n", i + 1));
                contextBuilder.append("질문: ").append(doc.getQuestion()).append("\n");
                contextBuilder.append("답변: ").append(doc.getAnswer()).append("\n\n");
            }
            
            // LLM 프롬프트 구성
            String prompt = String.format(
                "%s\n\n위 FAQ 문서들을 참고하여, 다음 사용자 질문에 대해 친절하고 정확하게 답변해주세요.\n" +
                "답변은 한국어로 작성하고, FAQ 문서의 내용을 바탕으로 하지만 더 자연스럽고 이해하기 쉽게 설명해주세요.\n" +
                "만약 FAQ 문서에 관련 정보가 충분하지 않다면, 그 점을 명확히 알려주세요.\n\n" +
                "사용자 질문: %s\n\n답변:",
                contextBuilder.toString(),
                userQuestion
            );
            
            String llmResponse = llmService.generateResponse(prompt);
            
            if (llmResponse != null && !llmResponse.trim().isEmpty()) {
                logger.info("LLM RAG 응답 생성 성공: 응답 길이={}자", llmResponse.length());
                // LLM 응답을 정리 (불필요한 앞뒤 공백 제거)
                String cleanedResponse = llmResponse.trim();
                // 첫 줄의 불필요한 설명 제거 (예: "답변:", "- " 등)
                if (cleanedResponse.startsWith("답변:")) {
                    cleanedResponse = cleanedResponse.substring(3).trim();
                }
                if (cleanedResponse.startsWith("- ")) {
                    cleanedResponse = cleanedResponse.substring(2).trim();
                }
                
                // 응답이 잘렸는지 확인하고 처리
                cleanedResponse = checkAndFixTruncatedResponse(cleanedResponse);
                
                return new ChatbotResponse(
                    cleanedResponse,
                    searchResults.get(0).getAnswer(), // 원본 FAQ 답변도 alternative로 제공
                    searchResults.get(0).getScore() * 0.8, // LLM 사용 시 약간 낮은 신뢰도 표시
                    true
                );
            } else {
                // LLM 실패 시 상위 FAQ 답변 반환
                logger.warn("LLM 응답 생성 실패, ES 답변 반환");
                return new ChatbotResponse(
                    searchResults.get(0).getAnswer(),
                    null,
                    searchResults.get(0).getScore(),
                    true
                );
            }
            
        } catch (Exception e) {
            logger.error("RAG 응답 생성 중 오류: {}", e.getMessage(), e);
            // 오류 시 상위 FAQ 답변 반환
            return new ChatbotResponse(
                searchResults.get(0).getAnswer(),
                null,
                searchResults.get(0).getScore(),
                true
            );
        }
    }
    
    /**
     * 기본 응답 생성
     */
    private String generateDefaultResponse(String question) {
        // LLM이 활성화되어 있으면 LLM에 직접 질문
        if (llmEnabled && llmService != null) {
            try {
                String prompt = String.format(
                    "BPE Platform 시스템에 대해 다음 질문에 답변해주세요. " +
                    "만약 정확한 정보를 모른다면 그 점을 명확히 알려주세요.\n\n질문: %s\n\n답변:",
                    question
                );
                String llmResponse = llmService.generateResponse(prompt);
                if (llmResponse != null && !llmResponse.trim().isEmpty()) {
                    // 응답이 잘렸는지 확인하고 처리
                    return checkAndFixTruncatedResponse(llmResponse.trim());
                }
            } catch (Exception e) {
                logger.debug("LLM 기본 응답 생성 실패: {}", e.getMessage());
            }
        }
        
        return "죄송합니다. '" + question + "'에 대한 답변을 찾을 수 없습니다. " +
               "더 구체적인 질문이나 다른 키워드로 질문해주시면 도와드리겠습니다.";
    }
    
    /**
     * 응답이 잘렸는지 확인하고 처리
     * - 마지막 문장이 불완전하면 제거
     * - 불완전한 응답임을 표시
     */
    private String checkAndFixTruncatedResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        // 한국어 문장 종료 구두점
        String[] sentenceEndings = {".", "!", "?", "。", "！", "？", "다.", "니다.", "요.", "습니다.", "습니다!", "합니다."};
        
        // 마지막 문장이 완전한지 확인
        boolean hasCompleteSentence = false;
        for (String ending : sentenceEndings) {
            if (response.trim().endsWith(ending)) {
                hasCompleteSentence = true;
                break;
            }
        }
        
        // 마지막 문장이 불완전한 경우
        if (!hasCompleteSentence && response.length() > 20) {
            // 마지막 불완전한 문장 제거
            int lastSentenceEnd = -1;
            for (String ending : sentenceEndings) {
                int lastIndex = response.lastIndexOf(ending);
                if (lastIndex > lastSentenceEnd) {
                    lastSentenceEnd = lastIndex + ending.length();
                }
            }
            
            if (lastSentenceEnd > 0 && lastSentenceEnd < response.length()) {
                // 마지막 완전한 문장까지 사용
                String fixedResponse = response.substring(0, lastSentenceEnd).trim();
                logger.info("응답이 불완전하여 마지막 문장을 제거했습니다. 원본 길이: {}자 -> 수정 후: {}자", 
                           response.length(), fixedResponse.length());
                return fixedResponse;
            } else {
                // 완전한 문장을 찾지 못한 경우 안내 메시지 추가
                logger.warn("응답이 불완전할 수 있습니다. 마지막 문장이 완전하지 않습니다.");
                return response + "\n\n(참고: 답변이 중간에 잘렸을 수 있습니다. 더 구체적인 질문을 해주시면 더 정확한 답변을 드릴 수 있습니다.)";
            }
        }
        
        return response;
    }
    
    /**
     * FAQ 문서 모델
     */
    public static class FAQDocument {
        private String id;
        private String question;
        private String answer;
        private List<String> keywords;
        private String category;
        private Double score;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
    
    /**
     * 챗봇 응답 모델
     */
    public static class ChatbotResponse {
        private String answer;
        private String alternativeAnswer;
        private Double confidence;
        private boolean found;
        
        public ChatbotResponse(String answer, String alternativeAnswer, Double confidence, boolean found) {
            this.answer = answer;
            this.alternativeAnswer = alternativeAnswer;
            this.confidence = confidence;
            this.found = found;
        }
        
        // Getters and Setters
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public String getAlternativeAnswer() { return alternativeAnswer; }
        public void setAlternativeAnswer(String alternativeAnswer) { this.alternativeAnswer = alternativeAnswer; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }
}


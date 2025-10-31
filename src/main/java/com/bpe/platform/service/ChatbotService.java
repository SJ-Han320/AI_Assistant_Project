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
    
    @Value("${app.elasticsearch.chatbot.index}")
    private String faqIndex;
    
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
            
            // 스코어가 너무 낮으면 기본 응답
            if (bestMatch.getScore() < 0.3) {
                return new ChatbotResponse(
                    generateDefaultResponse(userQuestion),
                    bestMatch.getAnswer(),
                    bestMatch.getScore(),
                    false
                );
            }
            
            return new ChatbotResponse(
                bestMatch.getAnswer(),
                null,
                bestMatch.getScore(),
                true
            );
            
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
     * 기본 응답 생성
     */
    private String generateDefaultResponse(String question) {
        return "죄송합니다. '" + question + "'에 대한 답변을 찾을 수 없습니다. " +
               "더 구체적인 질문이나 다른 키워드로 질문해주시면 도와드리겠습니다.";
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


package com.bpe.platform.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 데이터 챗봇 서비스
 * Elasticsearch의 소셜 데이터를 검색하고 LLM을 활용한 RAG 방식으로 답변 생성
 */
@Service
public class DataChatbotService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataChatbotService.class);
    
    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;
    
    @Autowired(required = false)
    private LlmService llmService;
    
    @Value("${app.elasticsearch.data-chatbot.index-pattern:lucy_main_v1_*}")
    private String indexPattern;
    
    @Value("${app.llm.server.enabled:true}")
    private boolean llmEnabled;
    
    // 검색 결과 최대 개수
    private static final int MAX_SEARCH_RESULTS = 5;
    
    // 최소 스코어 임계값 (이 값 이상이어야 관련성 있는 결과로 간주)
    // 검색 단계에서 필터링
    private static final double MIN_SCORE_THRESHOLD = 0.5;
    
    // 참고 자료 표시 최소 스코어 임계값 (이 값 이상이어야 참고 자료로 표시)
    // 평균 스코어나 최고 스코어가 이 값을 넘어야 참고 자료 표시
    private static final double MIN_SOURCE_DISPLAY_THRESHOLD = 0.8;
    
    /**
     * 사용자 질문에 대한 답변 생성
     * RAG 방식: Elasticsearch에서 관련 문서 검색 → LLM에 컨텍스트 제공 → 답변 생성
     */
    public DataChatbotResponse answerQuestion(String userQuestion) {
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return new DataChatbotResponse(
                "질문을 입력해주세요.",
                Collections.emptyList(),
                0.0,
                false
            );
        }
        
        // ES 클라이언트가 없으면 기본 응답
        if (elasticsearchClient == null) {
            logger.warn("Elasticsearch 클라이언트가 null입니다. 데이터 챗봇 기능을 사용할 수 없습니다.");
            return new DataChatbotResponse(
                "죄송합니다. 현재 데이터 챗봇 기능을 사용할 수 없습니다. Elasticsearch 클러스터에 연결할 수 없습니다.",
                Collections.emptyList(),
                0.0,
                false
            );
        }
        
        logger.info("데이터 챗봇 질문 처리 시작: question={}", userQuestion);
        
        try {
            // 1. Elasticsearch에서 관련 문서 검색
            List<SocialDataDocument> searchResults = searchSocialData(userQuestion);
            
            if (searchResults.isEmpty()) {
                return generateNoResultsResponse(userQuestion);
            }
            
            // 2. LLM을 사용하여 RAG 방식으로 답변 생성
            if (llmEnabled && llmService != null) {
                return generateRagResponse(userQuestion, searchResults);
            } else {
                // LLM이 비활성화된 경우 검색 결과를 직접 반환
                return generateDirectResponse(searchResults);
            }
            
        } catch (IOException e) {
            logger.error("Elasticsearch 검색 오류: {}", e.getMessage(), e);
            return new DataChatbotResponse(
                "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                Collections.emptyList(),
                0.0,
                false
            );
        } catch (Exception e) {
            logger.error("데이터 챗봇 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return new DataChatbotResponse(
                "예기치 않은 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                Collections.emptyList(),
                0.0,
                false
            );
        }
    }
    
    /**
     * 소셜 데이터 검색
     */
    private List<SocialDataDocument> searchSocialData(String question) throws IOException {
        // 제목과 내용 필드에서 검색 (제목에 더 높은 가중치 부여)
        Query searchQuery = Query.of(q -> q
            .multiMatch(m -> m
                .query(question)
                .fields("an_title^3", "an_content^2", "wc_writer_nick^1", "wc_sitename^1")
                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                .fuzziness("AUTO")
            )
        );
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(indexPattern)
            .query(searchQuery)
            .size(MAX_SEARCH_RESULTS)
            .source(src -> src
                .filter(f -> f
                    .includes("an_title", "an_content", "au_url", "wc_writer_nick", 
                             "wc_sitename", "wc_boardname", "wc_writer_id", "in_date", "in_write_date")
                )
            )
        );
        
        SearchResponse<SocialDataDocument> response = elasticsearchClient.search(
            searchRequest, 
            SocialDataDocument.class
        );
        
        List<SocialDataDocument> results = new ArrayList<>();
        for (Hit<SocialDataDocument> hit : response.hits().hits()) {
            SocialDataDocument doc = hit.source();
            Double score = hit.score();
            if (doc != null && score != null && score >= MIN_SCORE_THRESHOLD) {
                doc.setScore(score);
                doc.setId(hit.id());
                doc.setIndex(hit.index());
                results.add(doc);
            }
        }
        
        logger.info("검색 결과: {}개 문서 발견", results.size());
        return results;
    }
    
    /**
     * RAG 방식으로 답변 생성
     * 검색된 문서들을 컨텍스트로 LLM에 제공하여 자연스러운 답변 생성
     */
    private DataChatbotResponse generateRagResponse(
            String userQuestion, 
            List<SocialDataDocument> searchResults) {
        try {
            // 상위 검색 결과들을 컨텍스트로 구성
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("다음은 소셜 데이터에서 검색된 관련 문서들입니다:\n\n");
            
            int contextCount = Math.min(MAX_SEARCH_RESULTS, searchResults.size());
            for (int i = 0; i < contextCount; i++) {
                SocialDataDocument doc = searchResults.get(i);
                contextBuilder.append(String.format("[문서 %d]\n", i + 1));
                if (doc.getTitle() != null && !doc.getTitle().isEmpty()) {
                    contextBuilder.append("제목: ").append(doc.getTitle()).append("\n");
                }
                if (doc.getContent() != null && !doc.getContent().isEmpty()) {
                    // 내용이 너무 길면 일부만 사용 (최대 500자)
                    String content = doc.getContent();
                    if (content.length() > 500) {
                        content = content.substring(0, 500) + "...";
                    }
                    contextBuilder.append("내용: ").append(content).append("\n");
                }
                if (doc.getWriterNick() != null && !doc.getWriterNick().isEmpty()) {
                    contextBuilder.append("작성자: ").append(doc.getWriterNick()).append("\n");
                }
                if (doc.getSiteName() != null && !doc.getSiteName().isEmpty()) {
                    contextBuilder.append("사이트: ").append(doc.getSiteName()).append("\n");
                }
                contextBuilder.append("\n");
            }
            
            // LLM 프롬프트 구성
            String prompt = String.format(
                "%s\n\n" +
                "위 문서들을 참고하여, 다음 사용자 질문에 대해 친절하고 정확하게 답변해주세요.\n" +
                "답변은 한국어로 작성하고, 문서의 내용을 바탕으로 하지만 더 자연스럽고 이해하기 쉽게 설명해주세요.\n" +
                "만약 문서에 관련 정보가 충분하지 않다면, 그 점을 명확히 알려주세요.\n" +
                "답변할 때 가능하면 구체적인 예시나 데이터를 포함해주세요.\n" +
                "답변은 완전한 문장으로 끝까지 작성해주세요.\n\n" +
                "사용자 질문: %s\n\n답변:",
                contextBuilder.toString(),
                userQuestion
            );
            
            // LLM 응답 생성 (기본 max-tokens 사용)
            String llmResponse = llmService.generateResponse(prompt);
            
            if (llmResponse != null && !llmResponse.trim().isEmpty()) {
                logger.info("LLM RAG 응답 생성 성공: 응답 길이={}자", llmResponse.length());
                
                // LLM 응답 정리
                String cleanedResponse = llmResponse.trim();
                
                // 불필요한 접두사 제거
                if (cleanedResponse.startsWith("답변:")) {
                    cleanedResponse = cleanedResponse.substring(3).trim();
                }
                if (cleanedResponse.startsWith("- ")) {
                    cleanedResponse = cleanedResponse.substring(2).trim();
                }
                
                // 응답이 잘렸는지 확인하고 처리
                cleanedResponse = checkAndFixTruncatedResponse(cleanedResponse);
                
                // 평균 스코어 및 최고 스코어 계산
                double avgScore = searchResults.stream()
                    .mapToDouble(SocialDataDocument::getScore)
                    .average()
                    .orElse(0.0);
                
                double maxScore = searchResults.stream()
                    .mapToDouble(SocialDataDocument::getScore)
                    .max()
                    .orElse(0.0);
                
                // 스코어가 임계값을 넘지 않으면 참고 자료를 필터링
                // 평균 스코어와 최고 스코어 중 하나라도 임계값을 넘으면 참고 자료 표시
                List<SocialDataDocument> filteredSources = new ArrayList<>();
                if (avgScore >= MIN_SOURCE_DISPLAY_THRESHOLD || maxScore >= MIN_SOURCE_DISPLAY_THRESHOLD) {
                    // 스코어가 높은 항목만 필터링 (임계값 이상)
                    filteredSources = searchResults.stream()
                        .filter(doc -> doc.getScore() != null && doc.getScore() >= MIN_SOURCE_DISPLAY_THRESHOLD)
                        .collect(java.util.stream.Collectors.toList());
                    
                    logger.info("참고 자료 표시: 원본 {}개 -> 필터링 후 {}개 (평균 스코어: {}, 최고 스코어: {})", 
                               searchResults.size(), filteredSources.size(), avgScore, maxScore);
                } else {
                    logger.info("참고 자료 숨김: 평균 스코어 {} 및 최고 스코어 {}가 임계값 {} 미만", 
                               avgScore, maxScore, MIN_SOURCE_DISPLAY_THRESHOLD);
                }
                
                return new DataChatbotResponse(
                    cleanedResponse,
                    filteredSources,
                    avgScore,
                    true
                );
            } else {
                // LLM 실패 시 검색 결과 기반 직접 응답
                logger.warn("LLM 응답 생성 실패, 검색 결과 기반 응답 반환");
                return generateDirectResponse(searchResults);
            }
            
        } catch (Exception e) {
            logger.error("RAG 응답 생성 중 오류: {}", e.getMessage(), e);
            // 오류 시 검색 결과 기반 직접 응답
            return generateDirectResponse(searchResults);
        }
    }
    
    /**
     * 검색 결과를 직접 반환 (LLM 없이)
     */
    private DataChatbotResponse generateDirectResponse(List<SocialDataDocument> searchResults) {
        if (searchResults.isEmpty()) {
            return generateNoResultsResponse("");
        }
        
        // 첫 번째 검색 결과의 제목과 내용을 기반으로 간단한 응답 생성
        SocialDataDocument firstDoc = searchResults.get(0);
        StringBuilder responseBuilder = new StringBuilder();
        
        responseBuilder.append("검색된 문서를 찾았습니다:\n\n");
        if (firstDoc.getTitle() != null) {
            responseBuilder.append("제목: ").append(firstDoc.getTitle()).append("\n");
        }
        if (firstDoc.getContent() != null) {
            String content = firstDoc.getContent();
            if (content.length() > 300) {
                content = content.substring(0, 300) + "...";
            }
            responseBuilder.append("내용: ").append(content);
        }
        
        double avgScore = searchResults.stream()
            .mapToDouble(SocialDataDocument::getScore)
            .average()
            .orElse(0.0);
        
        double maxScore = searchResults.stream()
            .mapToDouble(SocialDataDocument::getScore)
            .max()
            .orElse(0.0);
        
        // 스코어가 임계값을 넘지 않으면 참고 자료를 필터링
        List<SocialDataDocument> filteredSources = new ArrayList<>();
        if (avgScore >= MIN_SOURCE_DISPLAY_THRESHOLD || maxScore >= MIN_SOURCE_DISPLAY_THRESHOLD) {
            filteredSources = searchResults.stream()
                .filter(doc -> doc.getScore() != null && doc.getScore() >= MIN_SOURCE_DISPLAY_THRESHOLD)
                .collect(Collectors.toList());
            
            logger.info("참고 자료 표시 (직접 응답): 원본 {}개 -> 필터링 후 {}개 (평균 스코어: {}, 최고 스코어: {})", 
                       searchResults.size(), filteredSources.size(), avgScore, maxScore);
        } else {
            logger.info("참고 자료 숨김 (직접 응답): 평균 스코어 {} 및 최고 스코어 {}가 임계값 {} 미만", 
                       avgScore, maxScore, MIN_SOURCE_DISPLAY_THRESHOLD);
        }
        
        return new DataChatbotResponse(
            responseBuilder.toString(),
            filteredSources,
            avgScore,
            true
        );
    }
    
    /**
     * 검색 결과가 없을 때 응답
     */
    private DataChatbotResponse generateNoResultsResponse(String question) {
        String response;
        if (llmEnabled && llmService != null) {
            // LLM이 활성화되어 있으면 일반적인 답변 시도
            try {
                String prompt = String.format(
                    "다음 질문에 대해 답변해주세요. 만약 정확한 정보를 모른다면 그 점을 명확히 알려주세요.\n\n질문: %s\n\n답변:",
                    question
                );
                // LLM 응답 생성
                String llmResponse = llmService.generateResponse(prompt);
                
                // 응답이 잘렸는지 확인하고 처리
                if (llmResponse != null) {
                    llmResponse = checkAndFixTruncatedResponse(llmResponse.trim());
                }
                if (llmResponse != null && !llmResponse.trim().isEmpty()) {
                    response = llmResponse.trim();
                } else {
                    response = "죄송합니다. '" + question + "'에 대한 관련 데이터를 찾을 수 없습니다. " +
                              "다른 키워드로 질문해주시거나 더 구체적인 질문을 해주세요.";
                }
            } catch (Exception e) {
                logger.debug("LLM 기본 응답 생성 실패: {}", e.getMessage());
                response = "죄송합니다. '" + question + "'에 대한 관련 데이터를 찾을 수 없습니다. " +
                          "다른 키워드로 질문해주시거나 더 구체적인 질문을 해주세요.";
            }
        } else {
            response = "죄송합니다. '" + question + "'에 대한 관련 데이터를 찾을 수 없습니다. " +
                      "다른 키워드로 질문해주시거나 더 구체적인 질문을 해주세요.";
        }
        
        return new DataChatbotResponse(
            response,
            Collections.emptyList(),
            0.0,
            false
        );
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
     * 소셜 데이터 문서 모델
     */
    public static class SocialDataDocument {
        private String id;
        private String index;
        
        @JsonProperty("an_title")
        private String title;           // an_title
        
        @JsonProperty("an_content")
        private String content;         // an_content
        
        @JsonProperty("au_url")
        private String url;            // au_url
        
        @JsonProperty("wc_writer_nick")
        private String writerNick;     // wc_writer_nick
        
        @JsonProperty("wc_writer_id")
        private String writerId;       // wc_writer_id
        
        @JsonProperty("wc_sitename")
        private String siteName;       // wc_sitename
        
        @JsonProperty("wc_boardname")
        private String boardName;      // wc_boardname
        
        @JsonProperty("in_write_date")
        private Integer writeDate;     // in_write_date
        
        @JsonProperty("in_date")
        private Integer date;          // in_date
        
        private Double score;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getIndex() { return index; }
        public void setIndex(String index) { this.index = index; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getWriterNick() { return writerNick; }
        public void setWriterNick(String writerNick) { this.writerNick = writerNick; }
        
        public String getWriterId() { return writerId; }
        public void setWriterId(String writerId) { this.writerId = writerId; }
        
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        
        public String getBoardName() { return boardName; }
        public void setBoardName(String boardName) { this.boardName = boardName; }
        
        public Integer getWriteDate() { return writeDate; }
        public void setWriteDate(Integer writeDate) { this.writeDate = writeDate; }
        
        public Integer getDate() { return date; }
        public void setDate(Integer date) { this.date = date; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
    
    /**
     * 데이터 챗봇 응답 모델
     */
    public static class DataChatbotResponse {
        private String answer;
        private List<SocialDataDocument> sources;
        private Double confidence;
        private boolean found;
        
        public DataChatbotResponse(String answer, List<SocialDataDocument> sources, 
                                  Double confidence, boolean found) {
            this.answer = answer;
            this.sources = sources != null ? sources : Collections.emptyList();
            this.confidence = confidence;
            this.found = found;
        }
        
        // Getters and Setters
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        
        public List<SocialDataDocument> getSources() { return sources; }
        public void setSources(List<SocialDataDocument> sources) { this.sources = sources; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }
    }
}


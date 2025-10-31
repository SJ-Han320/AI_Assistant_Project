package com.bpe.platform.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Elasticsearch 인덱스 초기화 유틸리티
 * 애플리케이션 시작 시 FAQ 인덱스가 없으면 생성하고 샘플 데이터 추가
 */
@Component
public class ElasticsearchIndexInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIndexInitializer.class);
    
    @Autowired(required = false)
    private ElasticsearchClient elasticsearchClient;
    
    @Value("${app.elasticsearch.chatbot.index}")
    private String faqIndex;
    
    @PostConstruct
    public void initializeIndex() {
        if (elasticsearchClient == null) {
            logger.error("⚠️ Elasticsearch 클라이언트가 초기화되지 않았습니다. 챗봇 기능은 사용할 수 없습니다.");
            return;
        }
        
        try {
            logger.info("Elasticsearch 연결 테스트 시작...");
            // Elasticsearch 연결 테스트
            var infoResponse = elasticsearchClient.info();
            logger.info("✅ Elasticsearch 연결 성공! Cluster: {}, Version: {}", 
                infoResponse.clusterName(), infoResponse.version().number());
            
            // 인덱스 존재 확인
            boolean exists = elasticsearchClient.indices()
                .exists(ExistsRequest.of(e -> e.index(faqIndex)))
                .value();
            
            if (!exists) {
                logger.info("FAQ 인덱스가 존재하지 않아 생성합니다: {}", faqIndex);
                // 인덱스 생성
                createFAQIndex();
                
                // 샘플 FAQ 데이터 추가
                addSampleFAQData();
                
                logger.info("✅ FAQ 인덱스가 성공적으로 생성되었습니다: {}", faqIndex);
            } else {
                logger.info("ℹ️ FAQ 인덱스가 이미 존재합니다: {}", faqIndex);
            }
        } catch (Exception e) {
            // ES 연결 실패 시에도 애플리케이션은 실행되도록 처리
            logger.error("⚠️ Elasticsearch 연결 실패 (챗봇 기능은 사용할 수 없습니다): {}", e.getMessage(), e);
            logger.error("   ES 클러스터가 실행 중인지 확인하세요. Host: {}", 
                System.getProperty("app.elasticsearch.host", "http://192.168.125.64:9200"));
            // 애플리케이션 실행은 계속되도록 예외를 다시 던지지 않음
        }
    }
    
    private void createFAQIndex() throws IOException {
        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(i -> i
            .index(faqIndex)
            .mappings(m -> m
                .properties("id", p -> p.keyword(k -> k))
                .properties("question", p -> p.text(t -> t
                    .analyzer("standard")
                    .fields("keyword", f -> f.keyword(k -> k))
                ))
                .properties("answer", p -> p.text(t -> t
                    .analyzer("standard")
                ))
                .properties("keywords", p -> p.keyword(k -> k))
                .properties("category", p -> p.keyword(k -> k))
            )
        );
        
        elasticsearchClient.indices().create(createIndexRequest);
    }
    
    private void addSampleFAQData() throws IOException {
        List<SampleFAQ> sampleFAQs = Arrays.asList(
            new SampleFAQ(
                "1",
                "BPE Platform이 무엇인가요?",
                "BPE Platform은 팀 내부에서 사용하는 웹 애플리케이션 플랫폼입니다. 사용자 관리, 데이터 공급, 프로젝트 관리, 월간 보고서 생성 등의 기능을 제공합니다.",
                Arrays.asList("BPE", "Platform", "플랫폼", "소개", "뭐"),
                "소개"
            ),
            new SampleFAQ(
                "2",
                "프로젝트를 어떻게 생성하나요?",
                "프로젝트 생성은 '데이터 공급' 메뉴에서 '프로젝트 생성' 버튼을 클릭하여 가능합니다. 프로젝트명, 데이터 쿼리, 저장소 정보(호스트, 데이터베이스, 테이블, 사용자, 비밀번호), 요청 필드 목록을 입력하면 됩니다.",
                Arrays.asList("프로젝트", "생성", "만들기", "추가", "등록"),
                "사용법"
            ),
            new SampleFAQ(
                "3",
                "데이터 공급 기능은 무엇인가요?",
                "데이터 공급 기능은 Spark Task를 통해 데이터를 추출하고 관리하는 기능입니다. 프로젝트를 생성하면 spark_task 테이블에 저장되며, 진행 상태를 실시간으로 확인할 수 있습니다.",
                Arrays.asList("데이터", "공급", "추출", "Spark", "Task"),
                "기능"
            ),
            new SampleFAQ(
                "4",
                "월간 보고 PPT는 어떻게 생성하나요?",
                "월간 보고 PPT 생성은 '월간 보고 PPT 생성' 메뉴에서 'PPT 생성' 버튼을 클릭하면 됩니다. 단, MANAGER 또는 ADMIN 권한이 필요합니다. PPT 생성에는 약 1~2분이 소요됩니다.",
                Arrays.asList("월간", "보고", "PPT", "생성", "파워포인트", "보고서"),
                "기능"
            ),
            new SampleFAQ(
                "5",
                "멤버 관리는 어떻게 하나요?",
                "멤버 관리 메뉴에서 사용자 계정을 추가, 수정, 삭제할 수 있습니다. 또한 사용자 권한(ADMIN, MANAGER, USER)을 설정할 수 있습니다.",
                Arrays.asList("멤버", "사용자", "관리", "계정", "권한"),
                "관리"
            ),
            new SampleFAQ(
                "6",
                "실시간 주요 키워드란 무엇인가요?",
                "실시간 주요 키워드는 6시간마다 업데이트되는 주유 핵심 키워드 상위 50개를 워드 클라우드와 리스트 형태로 보여주는 기능입니다.",
                Arrays.asList("키워드", "실시간", "워드클라우드", "트렌드"),
                "기능"
            ),
            new SampleFAQ(
                "7",
                "프로젝트 상태는 어떤 것들이 있나요?",
                "프로젝트 상태는 다음과 같습니다: 진행중(S), 완료(C), 대기(W), 오류(E). 데이터 공급 페이지에서 필터링하여 각 상태별 프로젝트를 확인할 수 있습니다.",
                Arrays.asList("상태", "진행중", "완료", "대기", "오류", "필터"),
                "사용법"
            ),
            new SampleFAQ(
                "8",
                "개인정보는 어떻게 수정하나요?",
                "우측 상단의 사람 아이콘을 클릭하여 '개인정보 수정' 메뉴로 접근할 수 있습니다. 현재 비밀번호 확인 후 이름과 비밀번호를 변경할 수 있으며, 프로필 이미지도 업로드하거나 삭제할 수 있습니다.",
                Arrays.asList("개인정보", "수정", "프로필", "이미지", "비밀번호"),
                "사용법"
            )
        );
        
        for (SampleFAQ faq : sampleFAQs) {
            elasticsearchClient.index(i -> i
                .index(faqIndex)
                .id(faq.id)
                .document(faq.toMap())
            );
        }
        
        // 인덱스 새로고침
        elasticsearchClient.indices().refresh(r -> r.index(faqIndex));
    }
    
    private static class SampleFAQ {
        private String id;
        private String question;
        private String answer;
        private List<String> keywords;
        private String category;
        
        public SampleFAQ(String id, String question, String answer, List<String> keywords, String category) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.keywords = keywords;
            this.category = category;
        }
        
        public java.util.Map<String, Object> toMap() {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", id);
            map.put("question", question);
            map.put("answer", answer);
            map.put("keywords", keywords);
            map.put("category", category);
            return map;
        }
    }
}


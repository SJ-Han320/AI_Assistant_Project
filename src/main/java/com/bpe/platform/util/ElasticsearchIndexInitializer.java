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
                // 인덱스가 이미 존재하면 데이터만 업데이트하지 않음 (재시작 시마다 업데이트 불필요)
                logger.info("기존 인덱스를 사용합니다. 데이터 업데이트는 수동으로 진행하세요.");
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
        // 기존 FAQ 데이터 삭제 후 새로 추가
        deleteAllFAQData();
        
        List<SampleFAQ> sampleFAQs = Arrays.asList(
            // 1. 플랫폼 소개
            new SampleFAQ(
                "1",
                "BPE Platform이 무엇인가요?",
                "BPE Platform은 팀 내부에서 사용하는 웹 애플리케이션 플랫폼입니다. 멤버 관리, 데이터 공급 및 프로젝트 관리, 월간 보고서 자동 생성, 실시간 키워드 분석, AI 기반 시스템 챗봇 등의 기능을 제공합니다.",
                Arrays.asList("BPE", "Platform", "플랫폼", "소개", "뭐", "어떤"),
                "소개"
            ),
            
            // 2. 프로젝트 생성 상세
            new SampleFAQ(
                "2",
                "프로젝트를 어떻게 생성하나요?",
                "데이터 공급 메뉴에서 '프로젝트 생성' 버튼을 클릭하세요. 프로젝트명, 데이터 쿼리, 저장소 정보(Host, Database, Table, User, Password), 그리고 요청 필드 목록을 입력해야 합니다. 요청 필드는 좌측에서 선택하여 우측으로 이동시키는 방식으로 선택할 수 있으며, 전체 선택/전체 해제 기능도 제공됩니다.",
                Arrays.asList("프로젝트", "생성", "만들기", "추가", "등록", "방법"),
                "사용법"
            ),
            
            // 3. 데이터 공급 기능
            new SampleFAQ(
                "3",
                "데이터 공급 기능은 무엇인가요?",
                "데이터 공급은 Spark Task를 통해 데이터를 추출하고 관리하는 핵심 기능입니다. 프로젝트를 생성하면 spark_task 테이블에 저장되며, 프로젝트의 진행 상태(대기/진행중/완료/오류)를 실시간으로 모니터링할 수 있습니다. 프로젝트명을 클릭하면 상세 정보(쿼리, 진행률, 저장소 정보 등)를 확인할 수 있습니다.",
                Arrays.asList("데이터", "공급", "추출", "Spark", "Task", "기능"),
                "기능"
            ),
            
            // 4. 월간 보고 PPT 생성
            new SampleFAQ(
                "4",
                "월간 보고 PPT는 어떻게 생성하나요?",
                "월간 보고 PPT 생성 메뉴에서 'PPT 생성' 버튼을 클릭하면 Quetta Cluster 시스템 운영 현황을 기반으로 PowerPoint 파일이 자동 생성됩니다. 단, MANAGER 또는 ADMIN 권한이 필요하며, PPT 생성에는 약 1~2분이 소요됩니다. 생성이 완료되면 자동으로 다운로드됩니다.",
                Arrays.asList("월간", "보고", "PPT", "생성", "파워포인트", "보고서", "권한"),
                "기능"
            ),
            
            // 5. 멤버 관리 (개선)
            new SampleFAQ(
                "5",
                "멤버 관리 페이지에서는 무엇을 할 수 있나요?",
                "멤버 관리 메뉴에서는 등록된 모든 팀 멤버의 정보를 조회할 수 있습니다. 프로필 이미지, 사용자명, 이름, 이메일, 역할(ADMIN/MANAGER/USER), 활성 상태, 가입일 등의 정보가 테이블 형태로 표시됩니다. 현재는 조회 기능이 제공되며, 향후 추가/수정/삭제 기능이 확장될 예정입니다.",
                Arrays.asList("멤버", "사용자", "관리", "계정", "권한", "조회"),
                "관리"
            ),
            
            // 6. 실시간 주요 키워드
            new SampleFAQ(
                "6",
                "실시간 주요 키워드 페이지는 어떤 기능인가요?",
                "실시간 주요 키워드는 6시간마다 업데이트되는 주유 핵심 키워드 상위 50개를 워드 클라우드와 리스트 형태로 시각화하여 보여주는 기능입니다. 왼쪽에는 워드 클라우드가 표시되고, 오른쪽에는 키워드 순위 리스트가 표시됩니다. 워드 클라우드의 키워드를 클릭하면 해당 항목이 리스트에서 강조 표시됩니다.",
                Arrays.asList("키워드", "실시간", "워드클라우드", "트렌드", "순위", "리스트"),
                "기능"
            ),
            
            // 7. 프로젝트 상태 및 필터링
            new SampleFAQ(
                "7",
                "프로젝트 상태는 어떤 것들이 있고 어떻게 필터링하나요?",
                "프로젝트 상태는 다음과 같습니다: 전체 작업, 완료된 작업(C), 진행 중(S), 대기 중(W), 오류(E). 데이터 공급 페이지 상단의 필터 카드를 클릭하면 각 상태별로 프로젝트를 필터링할 수 있습니다. 선택된 필터는 흰색 테두리와 체크마크로 표시되어 직관적으로 확인할 수 있습니다.",
                Arrays.asList("상태", "진행중", "완료", "대기", "오류", "필터", "필터링"),
                "사용법"
            ),
            
            // 8. 개인정보 수정
            new SampleFAQ(
                "8",
                "개인정보는 어떻게 수정하나요?",
                "우측 상단의 사람 아이콘을 클릭하여 개인정보 수정 메뉴로 접근할 수 있습니다. 먼저 현재 비밀번호를 입력하여 인증해야 하며, 인증 성공 후 이름, 이메일, 새 비밀번호를 변경할 수 있습니다. 프로필 이미지는 이미지를 클릭하여 업로드하거나 X 버튼으로 삭제할 수 있으며, 모든 변경사항은 저장 버튼을 클릭해야 적용됩니다.",
                Arrays.asList("개인정보", "수정", "프로필", "이미지", "비밀번호", "이름", "이메일"),
                "사용법"
            ),
            
            // 9. 시스템 챗봇 사용법 (업데이트)
            new SampleFAQ(
                "9",
                "시스템 챗봇은 어떻게 사용하나요?",
                "시스템 챗봇은 화면 오른쪽 하단의 플로팅 버튼(채팅 아이콘)을 클릭하여 사용할 수 있습니다. 버튼을 클릭하면 작은 챗봇 창이 열리며, 여기서 시스템에 대한 질문을 할 수 있습니다. 챗봇은 Elasticsearch와 AI 모델을 결합한 RAG 방식으로 작동하며, FAQ 문서를 검색하고 필요시 AI가 자연스러운 답변을 생성합니다. 창 상단의 X 버튼을 클릭하면 챗봇 창을 닫을 수 있습니다.",
                Arrays.asList("챗봇", "시스템", "채팅", "질문", "답변", "사용법", "방법", "플로팅", "버튼"),
                "사용법"
            ),
            
            // 15. 데이터 챗봇 사용법 (신규 추가)
            new SampleFAQ(
                "15",
                "데이터 챗봇은 무엇이고 어떻게 사용하나요?",
                "데이터 챗봇은 소셜 데이터(블로그, 카페 등)를 기반으로 질문에 답변하는 AI 챗봇입니다. 사이드바의 '데이터 챗봇' 메뉴를 클릭하여 접근할 수 있습니다. 질문을 입력하면 Elasticsearch에서 관련 소셜 데이터를 검색하고, LLM이 그 내용을 바탕으로 답변을 생성합니다. 답변과 함께 참고 자료 링크가 제공되며, 클릭하면 원문을 확인할 수 있습니다. 참고 자료는 관련성이 높은 경우(스코어 0.8 이상)에만 표시됩니다.",
                Arrays.asList("데이터", "챗봇", "소셜", "데이터", "블로그", "카페", "참고", "자료"),
                "사용법"
            ),
            
            // 16. 대시보드 관리 (신규 추가)
            new SampleFAQ(
                "16",
                "대시보드는 어떻게 관리하나요?",
                "대시보드 메뉴에서 대시보드 카드를 추가, 삭제, 순서 변경할 수 있습니다. 추가: '대시보드 추가' 버튼을 클릭하여 아이콘, 이름, 설명, URL을 입력합니다. 삭제: 각 카드의 삭제 버튼을 클릭하고 확인하면 삭제됩니다. 순서 변경: 카드 왼쪽 상단의 핸들을 드래그하여 원하는 위치로 이동시킬 수 있습니다. 카드를 클릭하면 설정된 URL이 새 창에서 열립니다.",
                Arrays.asList("대시보드", "추가", "삭제", "순서", "변경", "드래그", "드롭", "관리"),
                "사용법"
            ),
            
            // 17. 프로젝트 생성 외부 API 연동 (신규 추가)
            new SampleFAQ(
                "17",
                "프로젝트 생성 시 외부 API는 어떻게 연동되나요?",
                "프로젝트 생성 시 외부 Spark Task 등록 API(http://192.168.125.24:8000/register)가 자동으로 호출됩니다. 프로젝트명, 쿼리, 저장소 정보, 필드 목록이 API로 전송되며, 성공 응답을 받아야만 데이터베이스에 프로젝트가 저장됩니다. API 호출 실패 시 프로젝트는 생성되지 않으며, 오류 메시지가 표시됩니다. 프로젝트 생성 중에는 로딩 모달이 표시되며, 완료까지 시간이 소요될 수 있습니다.",
                Arrays.asList("프로젝트", "생성", "API", "연동", "외부", "Spark", "Task", "등록"),
                "기능"
            ),
            
            // 10. 프로젝트 상세 정보 확인 (신규 추가)
            new SampleFAQ(
                "10",
                "프로젝트 상세 정보는 어떻게 확인하나요?",
                "데이터 공급 페이지의 테이블에서 프로젝트명을 클릭하면 프로젝트 상세 정보 모달이 표시됩니다. 이 모달에서는 프로젝트명, 사용자명, 상태, 진행률(색상으로 표시), 쿼리 정보, 그리고 저장소 정보(Host, Database, Table, User)를 확인할 수 있습니다. 진행률은 0-30% 빨간색, 31-70% 노란색, 71-100% 초록색으로 색상이 변합니다.",
                Arrays.asList("프로젝트", "상세", "정보", "확인", "모달", "진행률", "저장소"),
                "사용법"
            ),
            
            // 11. 저장소 정보 입력 (신규 추가)
            new SampleFAQ(
                "11",
                "프로젝트 생성 시 저장소 정보는 어떻게 입력하나요?",
                "프로젝트 생성 모달의 저장소 정보 섹션에서 Host, Database, Table, User, Password를 입력해야 합니다. 이 정보는 데이터 추출을 위해 사용되는 데이터베이스 연결 정보입니다. 모든 필드는 필수 입력 항목이므로 정확하게 입력해야 프로젝트가 정상적으로 생성됩니다.",
                Arrays.asList("저장소", "정보", "Host", "Database", "Table", "User", "Password", "입력"),
                "사용법"
            ),
            
            // 12. 요청 필드 선택 방법 (신규 추가)
            new SampleFAQ(
                "12",
                "요청 필드 목록에서 필드는 어떻게 선택하나요?",
                "프로젝트 생성 모달의 우측에 있는 요청 필드 목록에서 좌측 '사용 가능한 필드'에서 원하는 필드를 클릭하면 우측 '선택된 필드'로 이동합니다. 반대로 선택된 필드를 클릭하면 다시 사용 가능한 필드로 돌아갑니다. 전체 선택 버튼으로 모든 필드를 한 번에 선택하거나, 전체 해제 버튼으로 모든 선택을 취소할 수 있으며, 초기화 버튼으로 모든 설정을 원래대로 되돌릴 수 있습니다.",
                Arrays.asList("요청", "필드", "선택", "목록", "전체", "선택", "해제", "초기화"),
                "사용법"
            ),
            
            // 13. 로그인 방법 (신규 추가)
            new SampleFAQ(
                "13",
                "로그인은 어떻게 하나요?",
                "BPE Platform에 접속하면 자동으로 로그인 페이지로 이동합니다. 사용자명과 비밀번호를 입력하여 로그인할 수 있습니다. 현재는 로그인 상태 유지 기능을 제공하지 않으므로, 매번 로그인해야 합니다. 로그인 성공 후 메인 대시보드로 이동합니다.",
                Arrays.asList("로그인", "인증", "사용자명", "비밀번호", "접속"),
                "사용법"
            ),
            
            // 14. 권한별 차이점 (신규 추가)
            new SampleFAQ(
                "14",
                "ADMIN, MANAGER, USER 권한의 차이는 무엇인가요?",
                "권한에 따라 접근 가능한 기능이 다릅니다. ADMIN과 MANAGER는 월간 보고 PPT 생성 기능을 사용할 수 있으나, USER 권한은 해당 기능이 비활성화됩니다. 기타 기능들은 모든 권한에서 동일하게 사용할 수 있습니다.",
                Arrays.asList("권한", "ADMIN", "MANAGER", "USER", "차이", "기능"),
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
        
        logger.info("✅ {}개의 FAQ 데이터가 추가되었습니다.", sampleFAQs.size());
    }
    
    /**
     * 기존 FAQ 데이터 모두 삭제 (인덱스 재구성 시 사용)
     */
    private void deleteAllFAQData() throws IOException {
        try {
            // 인덱스의 모든 문서 삭제
            elasticsearchClient.deleteByQuery(d -> d
                .index(faqIndex)
                .query(q -> q.matchAll(m -> m))
            );
            
            // 인덱스 새로고침
            elasticsearchClient.indices().refresh(r -> r.index(faqIndex));
            logger.info("기존 FAQ 데이터 삭제 완료");
        } catch (Exception e) {
            logger.warn("기존 FAQ 데이터 삭제 중 오류 발생 (무시): {}", e.getMessage());
            // 삭제 실패해도 계속 진행
        }
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


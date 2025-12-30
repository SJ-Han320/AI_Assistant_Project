# BPE Platform

## 프로젝트 개요
팀 내부에서 사용할 웹 애플리케이션 플랫폼입니다.

## 주요 기능
- 사용자 로그인/인증 시스템 (Spring Security)
- 대시보드 관리 시스템 (추가, 삭제, 드래그 앤 드롭 순서 변경)
- 멤버 관리 시스템
- 프로젝트 관리 및 데이터 공급 (외부 API 연동)
  - DB 저장소 타입 지원 (MySQL)
  - ES 저장소 타입 지원 (Elasticsearch)
- 월간 보고서 자동 생성
- 실시간 키워드 분석
- AI 기반 시스템 챗봇 (RAG 방식)
- 데이터 챗봇 (소셜 데이터 기반 RAG 방식)
- 프로필 이미지 관리

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.2.0
- **Frontend**: Thymeleaf, Bootstrap 5, WordCloud2.js
- **Database**: MySQL 8.0
- **Search Engine**: Elasticsearch 8.12.2
- **LLM**: Qwen2.5-3B-Instruct (RAG 방식)
- **Build Tool**: Maven

## 데이터베이스 정보
- **Host**: 환경 변수 또는 `application-local.yml`에서 설정
- **Database**: BPE_STAGE
- **Username**: 환경 변수 또는 `application-local.yml`에서 설정
- **Password**: 환경 변수 또는 `application-local.yml`에서 설정 (Git에 올라가지 않음)

## 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Maven 3.6 이상
- MySQL 8.0 이상
- Elasticsearch 8.12.2 (챗봇 기능 사용 시)

### 애플리케이션 실행 방법

#### 1. 포트 8082 사용 가능 여부 확인 및 PID 추출
```bash
# Windows (PowerShell)
netstat -ano | findstr :8082 | findstr LISTENING

# Linux/Mac
netstat -tulpn | grep :8082
# 또는
lsof -i :8082
```

#### 2. 포트가 사용 중인 경우 프로세스 종료
```bash
# Windows (PowerShell)
# 1단계에서 확인한 PID를 사용하여 프로세스 종료
taskkill /PID [PID번호] /F

# 예시: PID가 26504인 경우
taskkill /PID 26504 /F

# Linux/Mac
kill -9 [PID번호]
```

#### 3. Java Home 설정 및 애플리케이션 실행
```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
./mvnw spring-boot:run

# Linux/Mac
export JAVA_HOME=/path/to/java17
./mvnw spring-boot:run
```

#### 4. 대안 실행 방법 (Maven 직접 사용)
```bash
# Java Home 설정 후
mvn clean compile
mvn spring-boot:run
```

#### 5. 자동화 스크립트 사용 (권장)
```bash
# Windows 배치 파일
start-app.bat

# Windows PowerShell 스크립트
.\start-app.ps1
```

**스크립트 기능:**
- 포트 8082 사용 여부 자동 확인
- 사용 중인 프로세스 자동 종료 옵션 제공
- Java Home 자동 설정
- 애플리케이션 실행 및 오류 처리

#### 6. 애플리케이션 재시작 (권장 방법)

**자동화 스크립트 사용 (가장 간단):**
```bash
# Windows (PowerShell)
.\restart-app.ps1
```

**자동화된 재시작 절차 (수동 실행):**

```powershell
# Windows (PowerShell) - 전체 재시작 프로세스
# 1단계: 실행 중인 프로세스 확인 및 종료
$portCheck = netstat -ano | findstr :8082 | findstr LISTENING
if ($portCheck) {
    $pid = ($portCheck -split '\s+')[-1]
    Write-Host "포트 8082에서 실행 중인 프로세스 발견 (PID: $pid). 종료합니다..."
    taskkill /PID $pid /F
    Start-Sleep -Seconds 2
}

# 2단계: 프로젝트 디렉토리로 이동
cd C:\Users\HSJ\bpe-platform

# 3단계: Java Home 설정
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# 4단계: 애플리케이션 재시작
if (Test-Path ".\start-app.ps1") { 
    .\start-app.ps1 
} else { 
    .\mvnw.cmd spring-boot:run 
}

# 5단계: 재시작 확인 (백그라운드 실행 시)
# Start-Sleep -Seconds 25; netstat -ano | findstr :8082 | findstr LISTENING
```

**수동 재시작 절차:**

```bash
# Windows (PowerShell)
# 1단계: 실행 중인 프로세스 확인 및 종료
netstat -ano | findstr :8082 | findstr LISTENING
# 출력된 PID를 확인한 후
taskkill /PID [PID번호] /F

# 2단계: 애플리케이션 재시작
cd C:\Users\HSJ\bpe-platform
if (Test-Path ".\start-app.ps1") { .\start-app.ps1 } else { $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"; .\mvnw.cmd spring-boot:run }

# 또는 자동화 스크립트 사용
.\start-app.ps1
```

**재시작 확인:**
```bash
# 재시작 후 약 25초 대기 후 포트 확인
Start-Sleep -Seconds 25; netstat -ano | findstr :8082 | findstr LISTENING
```

**참고:**
- 프로젝트 디렉토리: `C:\Users\HSJ\bpe-platform`
- Java Home 경로: `C:\Program Files\Java\jdk-17`
- 애플리케이션 포트: `8082`

### 접속 정보
- **로컬 개발**: http://localhost:8082
- **테스트 서버**: (추후 설정)

### 주의사항
- 애플리케이션은 **포트 8082**에서 실행됩니다
- Java 17이 설치되어 있어야 하며, JAVA_HOME이 올바르게 설정되어야 합니다
- MySQL 데이터베이스가 실행 중이어야 합니다
- 프로필 이미지는 `uploads/profiles/` 디렉토리에 저장됩니다 (Maven 컴파일 시 삭제되지 않음)

## 메뉴 기능 설명

### 1. 대시보드 (Dashboard)
- **기능**: 사용자별 대시보드 카드 관리 및 외부 대시보드 링크
- **동작**: 
  - 로그인 후 첫 화면
  - 사용자별 대시보드 카드 표시 (DB 연동)
  - 대시보드 추가: 아이콘 선택, 이름, 설명, URL 입력
  - 대시보드 삭제: 삭제 버튼 클릭 후 확인 모달
  - 드래그 앤 드롭: 카드 왼쪽 상단 핸들을 드래그하여 순서 변경
  - 카드 클릭: URL이 설정된 경우 새 창에서 해당 대시보드 열기
  - Font Awesome 아이콘 지원
  - 대시보드가 없을 경우 "등록된 대시보드가 없습니다" 메시지 표시

### 2. 멤버 관리 (Members Management)
- **기능**: 팀 멤버 정보 조회 및 관리
- **동작**:
  - 등록된 모든 멤버 목록을 테이블 형태로 표시
  - 프로필 이미지, 사용자명, 이름, 이메일, 역할, 상태, 가입일 정보 표시
  - 역할 구분: ADMIN, MANAGER, USER
  - 상태: 활성/비활성

### 3. 데이터 생성 (Data Generation)
- **기능**: Spark Task 기반 데이터 추출 및 프로젝트 관리
- **동작**:
  
  **3.1 프로젝트 생성**
  - "프로젝트 생성" 버튼 클릭 시 모달 열림
  - 입력 항목:
    - 프로젝트명 (`st_name`) - 한글 입력 불가, 영문/숫자/하이픈/언더스코어만 허용
    - 소스 인덱스 (`st_origin`) - 타겟 인덱스 입력
    - 데이터 쿼리 (`st_query`) - Elasticsearch 쿼리
    - 저장소 타입 (`st_type`) - DB, ES, Excel 중 선택
    - 저장소 정보 (저장소 타입에 따라 다름):
      - **DB 타입**:
        - Host (`st_host`)
        - Database (`st_db`)
        - Destination (`st_destination`) - 기존 Table 필드
        - User (`st_db_id`)
        - Password (`st_db_pw`)
      - **ES 타입**:
        - ES Target Cluster (`st_db`)
        - ES Target Hosts (`st_host`)
        - ES Target Index (`st_destination`)
        - User (`st_db_id`)
        - Password (`st_db_pw`)
    - 요청 필드 목록:
      - 사용 가능한 필드와 선택된 필드를 좌우 분할 표시
      - 클릭으로 필드 이동 (전체 선택/전체 해제/초기화 버튼 제공)
      - 선택된 필드는 콤마로 구분하여 `st_field`에 저장
  - **외부 API 연동**:
    - 프로젝트 생성 시 다음 순서로 진행:
      1. DB에 프로젝트 정보 저장 (`spark_task` 테이블에 insert)
      2. 생성된 `st_seq` 값을 외부 API로 전달
      3. 외부 API 호출 (MySQLConfig 또는 ElasticsearchConfig 형식)
      4. API 실패 시 DB에서 롤백 (삭제)
    - API 요청 형식:
      - **MySQLConfig**:
        ```json
        {
          "service": "mysql",
          "project_name": "프로젝트명",
          "es_source_index": "소스 인덱스",
          "query": "쿼리 문자열",
          "mysql_host": "호스트",
          "mysql_database": "데이터베이스명",
          "mysql_table": "테이블명",
          "user": "사용자명",
          "password": "비밀번호",
          "fields": ["필드1", "필드2"],
          "st_seq": "생성된 시퀀스 번호"
        }
        ```
      - **ElasticsearchConfig**:
        ```json
        {
          "service": "elasticsearch",
          "project_name": "프로젝트명",
          "es_source_index": "소스 인덱스",
          "query": "쿼리 문자열",
          "es_target_hosts": "타겟 호스트",
          "es_target_index": "타겟 인덱스",
          "user": "사용자명",
          "password": "비밀번호",
          "fields": ["필드1", "필드2"],
          "st_seq": "생성된 시퀀스 번호"
        }
        ```
    - API 응답 확인: `{"result":{"state":"queued"}}` 또는 `{"message":"..."}` 형식 확인
    - 성공 시에만 DB에 레코드 유지, 실패 시 롤백
  - 입력 검증: 필수 필드 미입력, 필드 미선택 시 경고 모달 표시
  - 로딩 상태: 프로젝트 생성 중 "프로젝트 생성 중입니다..." 모달 표시
  - 오류 처리: 실패 시 "문제가 발생했으니 관리자에게 문의하세요" 모달 표시

  **3.2 프로젝트 목록 및 필터링**
  - 상태별 필터 카드:
    - 전체 작업 / 완료된 작업 (C) / 진행 중 (S) / 대기 중 (W) / 오류 (E)
    - 선택된 필터는 흰색 테두리, 체크마크, 그림자 효과로 표시
  - 테이블 컬럼:
    - 상태 (색상 코딩), 프로젝트명 (클릭 시 상세 정보), 사용자명, 등록시간 (`st_str_date`), 완료시간 (`st_end_date`)
  - 프로젝트명 클릭 시 상세 정보 모달 표시

  **3.3 프로젝트 상세 정보**
  - 모달에 표시되는 정보:
    - 프로젝트명, 사용자명
    - 상태 (아이콘 포함)
    - 진행률 (색상 변화: 0-30% 빨간색, 31-70% 노란색, 71-100% 초록색)
    - 소스 인덱스 (`st_origin`)
    - 쿼리 정보 (읽기 전용 텍스트 영역)
    - 저장소 정보 (저장소 타입에 따라 다르게 표시):
      - **DB 타입**: Host, Database, Table, User
      - **ES 타입**: Host, Cluster Name, Index, User
  - 진행률은 퍼센트와 함께 게이지바 중앙에 표시

### 4. 월간 보고 PPT 생성 (Monthly Report PPT Generation)
- **기능**: Quetta Cluster 시스템 운영 현황을 기반으로 PowerPoint 파일 자동 생성
- **동작**:
  - **권한 제한**: MANAGER 또는 ADMIN 역할만 PPT 생성 가능
  - "PPT 생성" 버튼 클릭 시:
    1. 서버 데이터 수집 (Python 스크립트 실행)
    2. 템플릿에 데이터 자동 삽입
    3. PowerPoint 파일 생성 (1~2분 소요)
    4. 완료 후 자동 다운로드
  - 생성된 파일은 `src/main/resources/monthly-report/` 디렉토리에 저장
- **참고**: 생성 중에는 로딩 모달이 표시되며, "PPT 생성까지 1~2분 소요됩니다." 안내 메시지 표시

### 5. 실시간 주요 키워드 (Real-time Major Keywords)
- **기능**: 6시간마다 업데이트되는 주유 핵심 키워드 상위 50개 시각화
- **동작**:
  - **워드 클라우드 (왼쪽 75%)**:
    - WordCloud2.js를 사용한 키워드 시각화
    - 키워드 빈도에 따라 크기와 색상 변화
    - 모든 텍스트는 가로 방향 표시
    - 키워드 클릭 시 해당 항목이 오른쪽 리스트에서 강조 표시 및 스크롤
  - **키워드 순위 리스트 (오른쪽 25%)**:
    - 순위 뱃지, 키워드명, 빈도(프로그레스 바) 표시
    - 스크롤 가능
  - **상호작용**: 
    - 워드 클라우드 키워드 클릭 → 리스트에서 해당 항목 강조 (파란색 배경, 흰색 테두리, 확대 효과)
    - 3초 후 자동으로 강조 해제

### 6. 시스템 챗봇 (System Chatbot)
- **기능**: RAG(Retrieval-Augmented Generation) 방식의 AI 챗봇
- **동작**:
  
  **6.1 검색 및 답변 생성 방식**
  - **1단계: Elasticsearch 검색**
    - 사용자 질문을 Elasticsearch FAQ 인덱스에서 검색
    - 하이브리드 검색: 텍스트 검색(BM25) + 키워드 매칭
    - 필드별 가중치: question(3.0), answer(2.0), keywords(1.5)
  
  **6.2 답변 생성 전략**
  - **ES 스코어 ≥ 0.4**: Elasticsearch에서 찾은 FAQ 답변 직접 반환 (빠른 응답)
  - **ES 스코어 < 0.4**: RAG 모드
    1. 상위 3개 FAQ 문서를 컨텍스트로 구성
    2. Qwen2.5-3B-Instruct LLM에 컨텍스트와 함께 질문 전달
    3. LLM이 자연스러운 답변 생성
  - **검색 결과 없음**: LLM에 직접 질문하여 시스템 전반 정보 기반 답변

  **6.3 사용자 인터페이스**
  - 카카오톡 스타일 채팅 UI
    - 사용자 메시지: 왼쪽, 파란색 그라데이션 버블, 프로필 이미지 표시
    - 챗봇 응답: 오른쪽, 어두운 회색 버블, 로봇 아이콘
  - 메시지 전송:
    - 입력 필드에 텍스트 입력 후 Enter 또는 전송 버튼 클릭
    - 전송 중 입력 필드 비활성화
    - 로딩 애니메이션 표시
  - 채팅 비우기:
    - 우측 상단 "채팅 비우기" 버튼 클릭
    - 확인 다이얼로그 후 모든 메시지 삭제 및 환영 메시지 재표시

  **6.4 Elasticsearch 연결 상태**
  - 챗봇 페이지 접속 시 자동으로 ES 연결 상태 확인
  - 연결 실패 시 빨간색 경고 메시지로 환영 메시지 대체
  - ES 연결 실패 응답도 빨간색 배경으로 강조 표시

### 7. 데이터 챗봇 (Data Chatbot)
- **기능**: 소셜 데이터 기반 RAG(Retrieval-Augmented Generation) 방식의 AI 챗봇
- **접근 권한**: 모든 사용자 접근 가능
- **동작**:
  
  **7.1 데이터 소스**
  - Elasticsearch의 소셜 데이터 인덱스 사용 (`lucy_main_bac1_*` 패턴)
  - 블로그, 카페 등 소셜 미디어 콘텐츠 데이터
  - 필드: 제목(`an_title`), 내용(`an_content`), URL(`au_url`), 작성자(`wc_writer_nick`), 사이트명(`wc_sitename`) 등

  **7.2 검색 및 답변 생성 방식**
  - **1단계: Elasticsearch 검색**
    - 사용자 질문을 소셜 데이터 인덱스에서 검색
    - Multi-match 검색: 제목(3.0배 가중치), 내용(2.0배 가중치), 작성자(1.0배), 사이트명(1.0배)
    - 상위 5개 관련 문서 추출 (최소 스코어 0.1 이상)
  
  **7.3 RAG 방식 답변 생성**
  - **검색 결과가 있는 경우**:
    1. 상위 5개 검색 문서의 제목, 내용, 작성자, 사이트 정보를 컨텍스트로 구성
    2. Qwen2.5-3B-Instruct LLM에 컨텍스트와 질문을 함께 전달
    3. LLM이 문서 내용을 바탕으로 자연스럽고 정확한 답변 생성
    4. 답변과 함께 참고 자료 링크 제공 (최대 5개)
  - **검색 결과가 없는 경우**:
    - LLM에 직접 질문하여 일반적인 답변 시도
    - 관련 데이터를 찾을 수 없다는 안내 메시지

  **7.4 사용자 인터페이스**
  - 카카오톡 스타일 채팅 UI
    - 사용자 메시지: 왼쪽, 파란색 그라데이션 버블, 프로필 이미지 표시
    - 챗봇 응답: 오른쪽, 파란색 그라데이션 버블, 데이터베이스 아이콘
  - 참고 자료 표시:
    - 답변 하단에 "📚 참고 자료 (N개)" 섹션 표시
    - 각 참고 자료: 제목, 사이트명, 작성자, 원문 링크
    - 원문 링크 클릭 시 새 탭에서 원본 콘텐츠 열기
  - 메시지 전송:
    - 입력 필드에 질문 입력 후 Enter 또는 전송 버튼 클릭
    - 전송 중 "데이터를 검색하고 답변을 생성하는 중입니다..." 로딩 메시지 표시
  - 채팅 비우기:
    - 우측 상단 "채팅 비우기" 버튼 클릭
    - 확인 다이얼로그 후 모든 메시지 삭제 및 환영 메시지 재표시

  **7.5 설정**
  - **인덱스 패턴**: `app.elasticsearch.data-chatbot.index-pattern` (기본값: `lucy_main_bac1_*`)
  - **LLM 서버**: Qwen2.5-3B-Instruct 모델 사용
  - **검색 결과 수**: 최대 5개 문서
  - **최소 스코어**: 0.1 이상의 검색 결과만 사용

### 8. 설정 (Settings)
- **기능**: 시스템 설정 (준비 중)
- **동작**: 현재 구현 예정 상태

## 추가 기능

### 개인정보 수정
- **접근**: 우측 상단 사용자 아이콘 클릭
- **기능**:
  - **1단계: 현재 비밀번호 확인**
    - 현재 비밀번호 입력
    - 확인 버튼 클릭
  - **2단계: 정보 수정**
    - 이름 수정
    - 이메일 수정
    - 새 비밀번호 변경 (선택)
    - 프로필 이미지 변경:
      - 이미지 클릭하여 파일 선택
      - X 버튼으로 이미지 삭제
    - 저장 버튼 클릭 시 모든 변경사항 일괄 적용
- **동작**:
  - 현재 비밀번호 확인 후에만 수정 가능
  - 프로필 이미지 변경/삭제는 저장 버튼 클릭 시에만 반영
  - 저장 시 사이드바 프로필 정보 즉시 업데이트

### 프로필 이미지 관리
- **저장 위치**: `uploads/profiles/` 디렉토리
- **파일명 형식**: `profile_{userId}_{UUID}.{확장자}`
- **지원 형식**: JPG, PNG, GIF, WebP
- **최대 크기**: 5MB
- **접근 경로**: `/images/profiles/{파일명}`
- **기본 이미지**: `/images/default/default-avatar.svg`

## 프로젝트 구조
```
bpe-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bpe/platform/
│   │   │       ├── BpePlatformApplication.java
│   │   │       ├── controller/         # REST API 및 페이지 컨트롤러
│   │   │       ├── service/            # 비즈니스 로직
│   │   │       ├── repository/        # 데이터 접근 계층
│   │   │       ├── entity/            # 엔티티 클래스
│   │   │       └── config/            # 설정 클래스
│   │   └── resources/
│   │       ├── application.yml        # 애플리케이션 설정
│   │       ├── templates/             # Thymeleaf 템플릿
│   │       ├── static/                # 정적 리소스
│   │       └── monthly-report/       # 월간 보고서 관련 파일
│   └── test/
├── uploads/                           # 업로드 파일 저장소
│   └── profiles/                      # 프로필 이미지
├── pom.xml
├── README.md
├── start-app.bat                      # Windows 배치 파일
├── start-app.ps1                      # Windows PowerShell 스크립트
└── restart-app.ps1                    # Windows PowerShell 재시작 스크립트
```

## 설정 파일

### application.yml 주요 설정

#### 데이터베이스 연결
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/BPE_STAGE?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    # serverTimezone=Asia/Seoul: 한국 시간대(KST) 사용
    # 실제 값은 환경 변수 또는 application-local.yml에서 설정
```

#### 프로필 이미지
```yaml
app:
  upload:
    dir: uploads/profiles/              # 프로필 이미지 저장 경로
```

#### Elasticsearch (챗봇)
```yaml
app:
  elasticsearch:
    host: ${ELASTICSEARCH_HOST:http://localhost:9200}
    username: ${ELASTICSEARCH_USERNAME:elastic}
    password: ${ELASTICSEARCH_PASSWORD:}
    chatbot:
      index: bpe_chatbot_faq            # FAQ 인덱스명 (시스템 챗봇용)
    data-chatbot:
      index-pattern: lucy_main_bac1_*   # 소셜 데이터 인덱스 패턴 (데이터 챗봇용)
    # 실제 값은 환경 변수 또는 application-local.yml에서 설정
```

#### LLM 서버 (챗봇 RAG)
```yaml
app:
  llm:
    server:
      url: ${LLM_SERVER_URL:http://localhost:8088}  # Qwen2.5 모델 서버
      enabled: true                     # LLM 활성화 여부
      timeout: 30000                    # 타임아웃 (ms)
      max-tokens: 300                   # 최대 토큰 수
      temperature: 0.2                  # 창의성 조절 (0.0-1.0)
```

#### 월간 보고서
```yaml
app:
  report:
    script:
      path: src/main/resources/monthly-report/getServerStatus.py
    template:
      path: src/main/resources/monthly-report/template/
    output:
      path: src/main/resources/monthly-report/
```

#### 외부 API 연동
```yaml
# 외부 API URL은 환경 변수로 설정하거나 코드에서 직접 설정
# 프로젝트 생성 시 외부 API로 전달되는 데이터 형식:
# - MySQLConfig: MySQL 데이터베이스 저장소 타입
# - ElasticsearchConfig: Elasticsearch 저장소 타입
```

## 데이터베이스 스키마

### 주요 테이블

#### users
- 사용자 정보 저장
- 컬럼: id, username, password, name, email, role, enabled, profile_image, created_at, updated_at

#### spark_task
- 프로젝트 및 Spark 작업 정보
- 컬럼: 
  - st_seq (PK, AUTO_INCREMENT)
  - st_name (프로젝트명)
  - st_query (쿼리)
  - st_origin (소스 인덱스)
  - st_type (저장소 타입: DB/ES/EX)
  - st_status (상태: W/S/C/E)
  - st_progress (진행률: 0-100)
  - st_user (사용자 ID)
  - st_host (호스트)
  - st_db (데이터베이스/클러스터명)
  - st_destination (테이블/인덱스명, 기존 st_table에서 변경)
  - st_db_id (사용자명)
  - st_db_pw (비밀번호)
  - st_field (선택된 필드 목록, 콤마 구분)
  - st_str_date (등록일시)
  - st_end_date (완료일시)

#### dashboard
- 사용자별 대시보드 카드 정보
- 컬럼: d_seq, d_user, d_name, d_description, d_icon, d_url, d_order, d_date
- d_user: 사용자 ID (users 테이블의 id와 연결)
- d_order: 대시보드 표시 순서 (오름차순 정렬)
- d_icon: Font Awesome 아이콘 클래스 (예: "fas fa-home")
- d_date: 대시보드 생성일시 (한국 시간 KST)

#### code
- 시스템 코드 관리
- 컬럼: c_type, c_value, c_name, c_order, c_use

## 개발 일정
- [x] 프로젝트 초기 설정
- [x] Spring Boot 프로젝트 구조 생성
- [x] 데이터베이스 연결 설정
- [x] 로그인 기능 구현
- [x] 메인 대시보드 구현
- [x] 멤버 관리 기능
- [x] 월간 보고 PPT 생성 기능
- [x] 데이터 공급 및 프로젝트 관리 기능
- [x] 프로젝트 생성 시 저장소 타입 지원 (DB/ES)
- [x] 프로젝트 생성 시 외부 API 연동 (MySQLConfig/ElasticsearchConfig)
- [x] 프로젝트 상세 정보 모달 개선 (저장소 타입별 정보 표시)
- [x] 실시간 주요 키워드 시각화
- [x] 시스템 챗봇 (RAG 방식)
- [x] 데이터 챗봇 (소셜 데이터 기반 RAG 방식)
- [x] 개인정보 수정 기능
- [x] 대시보드 관리 기능 (추가, 삭제, 순서 변경)
- [x] 모달 알림 시스템 (대시보드, 프로젝트 생성)
- [ ] 테스트 서버 배포

## 팀 정보
- 개발팀 내부 사용 웹 애플리케이션
- 로컬 개발 → 테스트 서버 배포 예정

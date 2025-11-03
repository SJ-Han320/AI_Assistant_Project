# BPE Platform

## 프로젝트 개요
팀 내부에서 사용할 웹 애플리케이션 플랫폼입니다.

## 주요 기능
- 사용자 로그인/인증 시스템 (Spring Security)
- 멤버 관리 시스템
- 프로젝트 관리 및 데이터 공급
- 월간 보고서 자동 생성
- 실시간 키워드 분석
- AI 기반 시스템 챗봇 (RAG 방식)
- 프로필 이미지 관리

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.2.0
- **Frontend**: Thymeleaf, Bootstrap 5, WordCloud2.js
- **Database**: MySQL 8.0
- **Search Engine**: Elasticsearch 8.12.2
- **LLM**: Qwen2.5-3B-Instruct (RAG 방식)
- **Build Tool**: Maven

## 데이터베이스 정보
- **Host**: 192.168.125.61
- **Database**: BPE_STAGE
- **Username**: root
- **Password**: dkfdptmdps404

## 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Maven 3.6 이상
- MySQL 8.0 이상
- Elasticsearch 8.12.2 (챗봇 기능 사용 시)

### 애플리케이션 실행 방법

#### 1. 포트 8082 사용 가능 여부 확인
```bash
# Windows (PowerShell)
netstat -ano | findstr :8082

# Linux/Mac
netstat -tulpn | grep :8082
# 또는
lsof -i :8082
```

#### 2. 포트가 사용 중인 경우 프로세스 종료
```bash
# Windows (PowerShell)
# netstat 결과에서 PID 확인 후
taskkill /PID [PID번호] /F

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
- **기능**: 메인 홈 화면
- **동작**: 
  - 로그인 후 첫 화면
  - 각 기능 메뉴로의 진입점 제공
  - 시스템 개요 정보 표시

### 2. 멤버 관리 (Members Management)
- **기능**: 팀 멤버 정보 조회 및 관리
- **동작**:
  - 등록된 모든 멤버 목록을 테이블 형태로 표시
  - 프로필 이미지, 사용자명, 이름, 이메일, 역할, 상태, 가입일 정보 표시
  - 역할 구분: ADMIN, MANAGER, USER
  - 상태: 활성/비활성

### 3. 월간 보고 PPT 생성 (Monthly Report PPT Generation)
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

### 4. 데이터 공급 (Data Supply)
- **기능**: Spark Task 기반 데이터 추출 및 프로젝트 관리
- **동작**:
  
  **4.1 프로젝트 생성**
  - "프로젝트 생성" 버튼 클릭 시 모달 열림
  - 입력 항목:
    - 프로젝트명 (`st_name`)
    - 데이터 쿼리 (`st_query`)
    - 저장소 정보:
      - Host (`st_host`)
      - Database (`st_db`)
      - Table (`st_table`)
      - User (`st_db_id`)
      - Password (`st_db_pw`)
    - 요청 필드 목록:
      - 사용 가능한 필드와 선택된 필드를 좌우 분할 표시
      - 클릭으로 필드 이동 (전체 선택/전체 해제/초기화 버튼 제공)
      - 선택된 필드는 콤마로 구분하여 `st_field`에 저장
  - 저장 시 `spark_task` 테이블에 레코드 생성

  **4.2 프로젝트 목록 및 필터링**
  - 상태별 필터 카드:
    - 전체 작업 / 완료된 작업 (C) / 진행 중 (S) / 대기 중 (W) / 오류 (E)
    - 선택된 필터는 흰색 테두리, 체크마크, 그림자 효과로 표시
  - 테이블 컬럼:
    - 상태 (색상 코딩), 프로젝트명 (클릭 시 상세 정보), 사용자명, 등록시간 (`st_str_date`), 완료시간 (`st_end_date`)
  - 프로젝트명 클릭 시 상세 정보 모달 표시

  **4.3 프로젝트 상세 정보**
  - 모달에 표시되는 정보:
    - 프로젝트명, 사용자명
    - 상태 (아이콘 포함)
    - 진행률 (색상 변화: 0-30% 빨간색, 31-70% 노란색, 71-100% 초록색)
    - 쿼리 정보 (읽기 전용 텍스트 영역)
    - 저장소 정보: Host, Database, Table, User
  - 진행률은 퍼센트와 함께 게이지바 중앙에 표시

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

### 7. 데이터 분석 (Analytics)
- **기능**: 데이터 분석 도구 (준비 중)
- **동작**: 현재 구현 예정 상태

### 8. 리포트 (Reports)
- **기능**: 리포트 조회 (준비 중)
- **동작**: 현재 구현 예정 상태

### 9. 알림 (Notifications)
- **기능**: 알림 관리 (준비 중)
- **동작**: 현재 구현 예정 상태

### 10. 히스토리 (History)
- **기능**: 작업 히스토리 조회 (준비 중)
- **동작**: 현재 구현 예정 상태

### 11. 설정 (Settings)
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
│   │       └── monthly-report/        # 월간 보고서 관련 파일
│   └── test/
├── uploads/                           # 업로드 파일 저장소
│   └── profiles/                      # 프로필 이미지
├── pom.xml
├── README.md
├── start-app.bat                      # Windows 배치 파일
└── start-app.ps1                      # Windows PowerShell 스크립트
```

## 설정 파일

### application.yml 주요 설정

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
    host: http://192.168.125.64:9200
    username: elastic
    password: elastic
    chatbot:
      index: bpe_chatbot_faq            # FAQ 인덱스명
```

#### LLM 서버 (챗봇 RAG)
```yaml
app:
  llm:
    server:
      url: http://192.168.125.70:8088   # Qwen2.5 모델 서버
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

## 데이터베이스 스키마

### 주요 테이블

#### users
- 사용자 정보 저장
- 컬럼: id, username, password, name, email, role, enabled, profile_image, created_at, updated_at

#### spark_task
- 프로젝트 및 Spark 작업 정보
- 컬럼: st_seq, st_name, st_query, st_status, st_progress, st_user, st_host, st_db, st_table, st_db_id, st_db_pw, st_field, st_str_date, st_end_date

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
- [x] 실시간 주요 키워드 시각화
- [x] 시스템 챗봇 (RAG 방식)
- [x] 개인정보 수정 기능
- [ ] 테스트 서버 배포

## 팀 정보
- 개발팀 내부 사용 웹 애플리케이션
- 로컬 개발 → 테스트 서버 배포 예정

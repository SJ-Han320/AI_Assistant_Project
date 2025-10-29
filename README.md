# BPE Platform

## 프로젝트 개요
팀 내부에서 사용할 웹 애플리케이션 플랫폼입니다.

## 주요 기능
- 사용자 로그인/인증 시스템
- 메인 대시보드 (8개 기능 버튼)
- 팀 협업 도구들

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.x
- **Frontend**: Thymeleaf, Bootstrap
- **Database**: MySQL 8.0
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

## 프로젝트 구조
```
bpe-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bpe/platform/
│   │   │       ├── BpePlatformApplication.java
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/
│   └── test/
├── pom.xml
├── README.md
├── start-app.bat          # Windows 배치 파일
└── start-app.ps1          # Windows PowerShell 스크립트
```

## 개발 일정
- [x] 프로젝트 초기 설정
- [ ] Spring Boot 프로젝트 구조 생성
- [ ] 데이터베이스 연결 설정
- [ ] 로그인 기능 구현
- [ ] 메인 대시보드 구현
- [ ] 테스트 서버 배포

## 팀 정보
- 개발팀 내부 사용 웹 애플리케이션
- 로컬 개발 → 테스트 서버 배포 예정
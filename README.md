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

### 로컬 개발 실행
```bash
# 프로젝트 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

### 접속 정보
- **로컬 개발**: http://localhost:8080
- **테스트 서버**: (추후 설정)

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
└── README.md
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
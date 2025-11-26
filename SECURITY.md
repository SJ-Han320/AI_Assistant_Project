# 보안 가이드

## 민감한 정보 관리

이 프로젝트는 민감한 정보(DB 비밀번호, API 키 등)를 Git에 올리지 않도록 설계되었습니다.

## 설정 방법

### 방법 1: application-local.yml 사용 (권장)

1. `src/main/resources/application-local.yml.example` 파일을 복사
2. `application-local.yml`로 이름 변경
3. 실제 값으로 채우기

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

`application-local.yml`은 `.gitignore`에 포함되어 Git에 올라가지 않습니다.

### 방법 2: 환경 변수 사용

다음 환경 변수를 설정하세요:

- `DB_URL`: 데이터베이스 URL
- `DB_USERNAME`: 데이터베이스 사용자명
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `ELASTICSEARCH_HOST`: Elasticsearch 호스트
- `ELASTICSEARCH_USERNAME`: Elasticsearch 사용자명
- `ELASTICSEARCH_PASSWORD`: Elasticsearch 비밀번호
- `ALPHA_VANTAGE_API_KEY`: Alpha Vantage API 키
- `API_DB_URL`: API 데이터베이스 URL
- `API_DB_USERNAME`: API 데이터베이스 사용자명
- `API_DB_PASSWORD`: API 데이터베이스 비밀번호

### Windows (PowerShell)
```powershell
$env:DB_URL = "jdbc:mysql://192.168.125.69:3306/BPE_STAGE?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_password"
```

### Linux/Mac
```bash
export DB_URL="jdbc:mysql://192.168.125.69:3306/BPE_STAGE?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
```

## 주의사항

⚠️ **절대로 다음 파일들을 Git에 커밋하지 마세요:**
- `application-local.yml`
- `application-local.properties`
- `.env` 파일
- 실제 비밀번호가 포함된 모든 파일

## 이미 커밋된 경우

만약 이미 민감한 정보를 Git에 커밋했다면:

1. 즉시 비밀번호 변경
2. Git 히스토리에서 제거 (git filter-branch 또는 BFG Repo-Cleaner 사용)
3. 모든 팀원에게 알리고 새 비밀번호 공유


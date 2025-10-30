# BPE Platform 배포 스크립트 (PowerShell)
# 사용법: .\deploy.ps1

Write-Host "=== BPE Platform 배포 시작 ===" -ForegroundColor Green

# 1. JAR 파일 빌드
Write-Host "1. JAR 파일 빌드 중..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 빌드 실패!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 빌드 완료!" -ForegroundColor Green

# 2. 서버로 JAR 파일 전송
Write-Host "2. 서버로 JAR 파일 전송 중..." -ForegroundColor Yellow
scp target/platform-0.0.1-SNAPSHOT.jar root@192.168.125.61:/opt/

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 파일 전송 실패!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 파일 전송 완료!" -ForegroundColor Green

# 3. 서버에서 필요한 디렉토리 생성 및 파일 복사
Write-Host "3. 서버에서 필요한 디렉토리 설정 중..." -ForegroundColor Yellow
ssh root@192.168.125.61 @"
mkdir -p /opt/bpe-platform/images/profiles
mkdir -p /opt/bpe-platform/monthly-report/template
mkdir -p /opt/bpe-platform/monthly-report/output
cp -r /opt/src/main/resources/monthly-report/* /opt/bpe-platform/monthly-report/ 2>/dev/null || true
"@

# 4. 서버에서 애플리케이션 재시작
Write-Host "4. 서버에서 애플리케이션 재시작 중..." -ForegroundColor Yellow
ssh root@192.168.125.61 "cd /opt && UPLOAD_DIR=/opt/bpe-platform/images/profiles/ REPORT_SCRIPT_PATH=/opt/bpe-platform/monthly-report/getServerStatus.py REPORT_TEMPLATE_PATH=/opt/bpe-platform/monthly-report/template/ REPORT_OUTPUT_PATH=/opt/bpe-platform/monthly-report/output/ java -jar platform-0.0.1-SNAPSHOT.jar --spring.profiles.active=server --server.port=8082"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 서버 재시작 실패!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 배포 완료!" -ForegroundColor Green
Write-Host "🌐 접속 URL: http://192.168.125.61:8082" -ForegroundColor Cyan

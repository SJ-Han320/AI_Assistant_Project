# BPE Platform 애플리케이션 재시작 스크립트
# README.md의 재시작 절차를 자동화한 스크립트

Write-Host "=== BPE Platform 애플리케이션 재시작 ===" -ForegroundColor Cyan

# 프로젝트 디렉토리로 이동
$projectDir = "C:\Users\HSJ\bpe-platform"
if (Test-Path $projectDir) {
    Set-Location $projectDir
    Write-Host "프로젝트 디렉토리로 이동: $projectDir" -ForegroundColor Green
} else {
    Write-Host "오류: 프로젝트 디렉토리를 찾을 수 없습니다: $projectDir" -ForegroundColor Red
    exit 1
}

# 1단계: 실행 중인 프로세스 확인 및 종료
Write-Host "`n[1단계] 포트 8082 사용 중인 프로세스 확인 중..." -ForegroundColor Yellow
$portCheck = netstat -ano | findstr :8082 | findstr LISTENING

if ($portCheck) {
    $pid = ($portCheck -split '\s+')[-1]
    Write-Host "포트 8082에서 실행 중인 프로세스 발견 (PID: $pid)" -ForegroundColor Yellow
    Write-Host "프로세스 종료 중..." -ForegroundColor Yellow
    taskkill /PID $pid /F
    if ($LASTEXITCODE -eq 0) {
        Write-Host "프로세스가 성공적으로 종료되었습니다." -ForegroundColor Green
        Start-Sleep -Seconds 2
    } else {
        Write-Host "프로세스 종료 실패. 계속 진행합니다..." -ForegroundColor Yellow
    }
} else {
    Write-Host "포트 8082에서 실행 중인 프로세스가 없습니다." -ForegroundColor Green
}

# 2단계: Java Home 설정
Write-Host "`n[2단계] Java Home 설정 중..." -ForegroundColor Yellow
$javaHome = "C:\Program Files\Java\jdk-17"
if (Test-Path $javaHome) {
    $env:JAVA_HOME = $javaHome
    Write-Host "JAVA_HOME 설정 완료: $javaHome" -ForegroundColor Green
} else {
    Write-Host "경고: Java Home 경로를 찾을 수 없습니다: $javaHome" -ForegroundColor Yellow
    Write-Host "기존 JAVA_HOME을 사용합니다: $env:JAVA_HOME" -ForegroundColor Yellow
}

# 3단계: 애플리케이션 재시작
Write-Host "`n[3단계] 애플리케이션 재시작 중..." -ForegroundColor Yellow
if (Test-Path ".\quick-start.ps1") {
    Write-Host "quick-start.ps1 스크립트를 사용하여 애플리케이션을 시작합니다." -ForegroundColor Green
    .\quick-start.ps1
} elseif (Test-Path ".\start-app.ps1") {
    Write-Host "start-app.ps1 스크립트를 자동 모드로 사용하여 애플리케이션을 시작합니다." -ForegroundColor Green
    .\start-app.ps1 -Auto
} else {
    Write-Host "mvnw.cmd를 사용하여 애플리케이션을 시작합니다." -ForegroundColor Green
    .\mvnw.cmd spring-boot:run
}

Write-Host "`n애플리케이션 재시작 프로세스가 시작되었습니다." -ForegroundColor Cyan
Write-Host "약 25초 후 포트 8082에서 실행 여부를 확인하세요." -ForegroundColor Cyan
Write-Host "확인 명령어: netstat -ano | findstr :8082 | findstr LISTENING" -ForegroundColor Cyan


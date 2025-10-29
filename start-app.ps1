# BPE Platform 애플리케이션 시작 스크립트
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BPE Platform 애플리케이션 시작" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. 포트 8082 사용 가능 여부 확인 중..." -ForegroundColor Yellow

$portCheck = netstat -ano | findstr :8082
if ($portCheck) {
    Write-Host "경고: 포트 8082가 이미 사용 중입니다." -ForegroundColor Red
    Write-Host "사용 중인 프로세스:" -ForegroundColor Red
    Write-Host $portCheck -ForegroundColor Red
    
    $choice = Read-Host "사용 중인 프로세스를 종료하시겠습니까? (Y/N)"
    if ($choice -eq "Y" -or $choice -eq "y") {
        $processes = netstat -ano | findstr :8082 | ForEach-Object {
            $parts = $_ -split '\s+'
            $pid = $parts[-1]
            if ($pid -match '^\d+$') {
                Write-Host "프로세스 $pid 종료 중..." -ForegroundColor Yellow
                taskkill /PID $pid /F
            }
        }
        Start-Sleep -Seconds 2
    } else {
        Write-Host "애플리케이션을 종료합니다." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
}

Write-Host ""
Write-Host "2. Java Home 설정 중..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green

Write-Host ""
Write-Host "3. 애플리케이션 시작 중..." -ForegroundColor Yellow
Write-Host "접속 주소: http://localhost:8082" -ForegroundColor Green
Write-Host ""
Write-Host "애플리케이션을 종료하려면 Ctrl+C를 누르세요." -ForegroundColor Cyan
Write-Host ""

try {
    ./mvnw spring-boot:run
} catch {
    Write-Host "애플리케이션 실행 중 오류가 발생했습니다: $($_.Exception.Message)" -ForegroundColor Red
}

Read-Host "Press Enter to exit"

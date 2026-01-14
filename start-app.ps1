# BPE Platform 애플리케이션 시작 스크립트
param(
    [switch]$Auto,  # 자동 모드: 포트 사용 중이면 자동 종료
    [switch]$Force  # 강제 모드: 포트 사용 중이면 자동 종료 (Auto와 동일)
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BPE Platform 애플리케이션 시작" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. 포트 8082 사용 가능 여부 확인 중..." -ForegroundColor Yellow

$portCheck = netstat -ano | findstr :8082 | findstr LISTENING
if ($portCheck) {
    Write-Host "경고: 포트 8082가 이미 사용 중입니다." -ForegroundColor Red
    Write-Host "사용 중인 프로세스:" -ForegroundColor Red
    Write-Host $portCheck -ForegroundColor Red
    
    $autoKill = $Auto -or $Force
    
    if (-not $autoKill) {
        $choice = Read-Host "사용 중인 프로세스를 종료하시겠습니까? (Y/N)"
        $autoKill = ($choice -eq "Y" -or $choice -eq "y")
    }
    
    if ($autoKill) {
        $processes = netstat -ano | findstr :8082 | findstr LISTENING | ForEach-Object {
            $parts = $_ -split '\s+'
            $pid = $parts[-1]
            if ($pid -match '^\d+$') {
                Write-Host "프로세스 $pid 종료 중..." -ForegroundColor Yellow
                taskkill /PID $pid /F 2>$null
            }
        }
        Start-Sleep -Seconds 2
        Write-Host "프로세스 종료 완료." -ForegroundColor Green
    } else {
        Write-Host "애플리케이션을 종료합니다." -ForegroundColor Red
        if (-not $Auto) {
            Read-Host "Press Enter to exit"
        }
        exit 1
    }
} else {
    Write-Host "포트 8082 사용 가능." -ForegroundColor Green
}

Write-Host ""
Write-Host "2. Java Home 설정 중..." -ForegroundColor Yellow
$javaHome = "C:\Program Files\Java\jdk-17"
if (Test-Path $javaHome) {
    $env:JAVA_HOME = $javaHome
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "경고: Java Home 경로를 찾을 수 없습니다: $javaHome" -ForegroundColor Yellow
    if ($env:JAVA_HOME) {
        Write-Host "기존 JAVA_HOME 사용: $env:JAVA_HOME" -ForegroundColor Yellow
    } else {
        Write-Host "오류: JAVA_HOME이 설정되지 않았습니다." -ForegroundColor Red
        if (-not $Auto) {
            Read-Host "Press Enter to exit"
        }
        exit 1
    }
}

Write-Host ""
Write-Host "3. 애플리케이션 시작 중..." -ForegroundColor Yellow
Write-Host "접속 주소: http://localhost:8082" -ForegroundColor Green
Write-Host ""
Write-Host "애플리케이션을 종료하려면 Ctrl+C를 누르세요." -ForegroundColor Cyan
Write-Host ""

try {
    if (Test-Path ".\mvnw.cmd") {
        .\mvnw.cmd spring-boot:run
    } elseif (Test-Path ".\mvnw") {
        .\mvnw spring-boot:run
    } else {
        Write-Host "오류: mvnw 파일을 찾을 수 없습니다." -ForegroundColor Red
        if (-not $Auto) {
            Read-Host "Press Enter to exit"
        }
        exit 1
    }
} catch {
    Write-Host "애플리케이션 실행 중 오류가 발생했습니다: $($_.Exception.Message)" -ForegroundColor Red
    if (-not $Auto) {
        Read-Host "Press Enter to exit"
    }
    exit 1
}

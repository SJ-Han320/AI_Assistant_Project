# BPE Platform 빠른 시작 스크립트 (자동 모드)
# 포트 사용 중이면 자동으로 종료하고 실행합니다.

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BPE Platform 빠른 시작 (자동 모드)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 프로젝트 디렉토리로 이동
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# start-app.ps1를 자동 모드로 실행
if (Test-Path ".\start-app.ps1") {
    .\start-app.ps1 -Auto
} else {
    Write-Host "오류: start-app.ps1를 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}


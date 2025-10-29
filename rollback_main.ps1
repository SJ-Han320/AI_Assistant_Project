# 메인페이지 롤백 스크립트
Write-Host "메인페이지를 이전 버전으로 롤백합니다..." -ForegroundColor Yellow

# 백업 파일이 존재하는지 확인
if (Test-Path "src\main\resources\templates\main_backup.html") {
    # 현재 메인페이지를 백업
    Copy-Item "src\main\resources\templates\main.html" "src\main\resources\templates\main_current_backup.html" -Force
    Write-Host "현재 메인페이지를 main_current_backup.html로 백업했습니다." -ForegroundColor Green
    
    # 이전 버전으로 복원
    Copy-Item "src\main\resources\templates\main_backup.html" "src\main\resources\templates\main.html" -Force
    Write-Host "메인페이지가 이전 버전으로 롤백되었습니다." -ForegroundColor Green
    Write-Host "애플리케이션을 재시작하면 변경사항이 적용됩니다." -ForegroundColor Cyan
} else {
    Write-Host "백업 파일을 찾을 수 없습니다: main_backup.html" -ForegroundColor Red
    Write-Host "롤백을 수행할 수 없습니다." -ForegroundColor Red
}

Write-Host "`n롤백 완료!" -ForegroundColor Green

@echo off
echo ========================================
echo BPE Platform 애플리케이션 시작
echo ========================================

echo.
echo 1. 포트 8082 사용 가능 여부 확인 중...
netstat -ano | findstr :8082
if %errorlevel% equ 0 (
    echo 경고: 포트 8082가 이미 사용 중입니다.
    echo 사용 중인 프로세스를 종료하시겠습니까? (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082') do (
            echo 프로세스 %%a 종료 중...
            taskkill /PID %%a /F
        )
    ) else (
        echo 애플리케이션을 종료합니다.
        pause
        exit /b 1
    )
)

echo.
echo 2. Java Home 설정 중...
set JAVA_HOME=C:\Program Files\Java\jdk-17
echo JAVA_HOME: %JAVA_HOME%

echo.
echo 3. 애플리케이션 시작 중...
echo 접속 주소: http://localhost:8082
echo.
echo 애플리케이션을 종료하려면 Ctrl+C를 누르세요.
echo.

mvnw spring-boot:run

pause

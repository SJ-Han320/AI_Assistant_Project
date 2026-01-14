@echo off
setlocal enabledelayedexpansion

echo ========================================
echo BPE Platform 애플리케이션 시작
echo ========================================

echo.
echo 1. 포트 8082 사용 가능 여부 확인 중...
netstat -ano | findstr :8082 | findstr LISTENING >nul
if %errorlevel% equ 0 (
    echo 경고: 포트 8082가 이미 사용 중입니다.
    echo.
    echo 자동 모드: 사용 중인 프로세스를 자동으로 종료합니다.
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082 ^| findstr LISTENING') do (
        echo 프로세스 %%a 종료 중...
        taskkill /PID %%a /F >nul 2>&1
    )
    timeout /t 2 /nobreak >nul
    echo 프로세스 종료 완료.
) else (
    echo 포트 8082 사용 가능.
)

echo.
echo 2. Java Home 설정 중...
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA_HOME: %JAVA_HOME%
) else (
    echo 경고: Java Home 경로를 찾을 수 없습니다: %JAVA_HOME%
    if defined JAVA_HOME (
        echo 기존 JAVA_HOME 사용: %JAVA_HOME%
    ) else (
        echo 오류: JAVA_HOME이 설정되지 않았습니다.
        pause
        exit /b 1
    )
)

echo.
echo 3. 애플리케이션 시작 중...
echo 접속 주소: http://localhost:8082
echo.
echo 애플리케이션을 종료하려면 Ctrl+C를 누르세요.
echo.

if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else if exist "mvnw" (
    call mvnw spring-boot:run
) else (
    echo 오류: mvnw 파일을 찾을 수 없습니다.
    pause
    exit /b 1
)

pause

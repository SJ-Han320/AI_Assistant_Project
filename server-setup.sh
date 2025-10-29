#!/bin/bash

# 서버 초기 설정 스크립트
# 서버에서 한 번만 실행하면 됩니다

echo "=== 서버 초기 설정 시작 ==="

# 1. 애플리케이션 디렉토리 생성
echo "1. 애플리케이션 디렉토리 생성..."
mkdir -p /opt/bpe-platform
cd /opt/bpe-platform

# 2. 애플리케이션 시작 스크립트 생성
echo "2. 애플리케이션 관리 스크립트 생성..."
cat > start.sh << 'EOF'
#!/bin/bash
cd /opt/bpe-platform
nohup java -jar platform-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
echo $! > app.pid
echo "애플리케이션이 시작되었습니다. PID: $(cat app.pid)"
EOF

# 3. 애플리케이션 중지 스크립트 생성
cat > stop.sh << 'EOF'
#!/bin/bash
cd /opt/bpe-platform
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    if ps -p $PID > /dev/null; then
        kill $PID
        echo "애플리케이션이 중지되었습니다. PID: $PID"
    else
        echo "애플리케이션이 이미 중지되어 있습니다."
    fi
    rm -f app.pid
else
    echo "PID 파일이 없습니다. 애플리케이션이 실행 중이지 않을 수 있습니다."
fi
EOF

# 4. 애플리케이션 재시작 스크립트 생성
cat > restart.sh << 'EOF'
#!/bin/bash
cd /opt/bpe-platform
echo "애플리케이션 중지 중..."
./stop.sh
sleep 2
echo "애플리케이션 시작 중..."
./start.sh
EOF

# 5. 실행 권한 부여
chmod +x start.sh stop.sh restart.sh

echo "✅ 서버 초기 설정 완료!"
echo ""
echo "사용 가능한 명령어:"
echo "  ./start.sh   - 애플리케이션 시작"
echo "  ./stop.sh    - 애플리케이션 중지"
echo "  ./restart.sh - 애플리케이션 재시작"
echo "  tail -f app.log - 로그 확인"

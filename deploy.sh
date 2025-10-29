#!/bin/bash

# BPE Platform 배포 스크립트
# 사용법: ./deploy.sh

echo "=== BPE Platform 배포 시작 ==="

# 1. JAR 파일 빌드
echo "1. JAR 파일 빌드 중..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "✅ 빌드 완료!"

# 2. 서버로 JAR 파일 전송
echo "2. 서버로 JAR 파일 전송 중..."
scp target/platform-0.0.1-SNAPSHOT.jar root@192.168.125.61:/opt/bpe-platform/

if [ $? -ne 0 ]; then
    echo "❌ 파일 전송 실패!"
    exit 1
fi

echo "✅ 파일 전송 완료!"

# 3. 서버에서 애플리케이션 재시작
echo "3. 서버에서 애플리케이션 재시작 중..."
ssh root@192.168.125.61 << 'EOF'
cd /opt/bpe-platform
./restart.sh
EOF

if [ $? -ne 0 ]; then
    echo "❌ 서버 재시작 실패!"
    exit 1
fi

echo "✅ 배포 완료!"
echo "🌐 접속 URL: http://192.168.125.61:9090"

#!/bin/bash

# 개발 환경 체크 스크립트
# 사용법: ./scripts/check-env.sh

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}개발 환경 체크${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Java 버전 체크
echo -e "${YELLOW}📌 Java 버전:${NC}"
if command -v java &> /dev/null; then
    java -version 2>&1 | head -1
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}✅ Java 21 이상 설치됨${NC}"
    else
        echo -e "${RED}❌ Java 21 이상이 필요합니다 (현재: $JAVA_VERSION)${NC}"
    fi
else
    echo -e "${RED}❌ Java가 설치되지 않았습니다${NC}"
fi
echo ""

# Docker 체크
echo -e "${YELLOW}📌 Docker:${NC}"
if command -v docker &> /dev/null; then
    docker --version
    if docker ps &> /dev/null; then
        echo -e "${GREEN}✅ Docker가 실행 중입니다${NC}"
    else
        echo -e "${RED}❌ Docker가 실행되지 않았습니다${NC}"
    fi
else
    echo -e "${RED}❌ Docker가 설치되지 않았습니다${NC}"
fi
echo ""

# Docker Compose 체크
echo -e "${YELLOW}📌 Docker Compose:${NC}"
if command -v docker-compose &> /dev/null; then
    docker-compose --version
    echo -e "${GREEN}✅ Docker Compose 설치됨${NC}"
else
    echo -e "${RED}❌ Docker Compose가 설치되지 않았습니다${NC}"
fi
echo ""

# Gradle 체크
echo -e "${YELLOW}📌 Gradle:${NC}"
if [ -f "./gradlew" ]; then
    ./gradlew --version | head -3
    echo -e "${GREEN}✅ Gradle Wrapper 사용 가능${NC}"
else
    echo -e "${RED}❌ Gradle Wrapper가 없습니다${NC}"
fi
echo ""

# 컨테이너 상태 체크
echo -e "${YELLOW}📌 Docker 컨테이너 상태:${NC}"
if docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep template &> /dev/null; then
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "NAMES|template"
    echo -e "${GREEN}✅ 컨테이너가 실행 중입니다${NC}"
else
    echo -e "${YELLOW}⚠️  실행 중인 컨테이너가 없습니다${NC}"
    echo -e "${YELLOW}   'make start' 명령으로 시작하세요${NC}"
fi
echo ""

# 포트 체크
echo -e "${YELLOW}📌 포트 사용 상태:${NC}"
check_port() {
    PORT=$1
    NAME=$2
    if lsof -i :$PORT &> /dev/null 2>&1 || netstat -an | grep :$PORT | grep LISTEN &> /dev/null 2>&1; then
        echo -e "${GREEN}✅ $NAME ($PORT) 사용 중${NC}"
    else
        echo -e "${YELLOW}⚠️  $NAME ($PORT) 사용 가능${NC}"
    fi
}

check_port 5432 "PostgreSQL"
check_port 6379 "Redis"
check_port 5050 "pgAdmin"
check_port 8080 "Application"
echo ""

echo -e "${BLUE}=========================================${NC}"
echo -e "${GREEN}환경 체크 완료!${NC}"
echo -e "${BLUE}=========================================${NC}"

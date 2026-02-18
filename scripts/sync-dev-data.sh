#!/bin/bash

# 개발 서버 데이터를 로컬로 동기화하는 스크립트
# 사용법: ./scripts/sync-dev-data.sh

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 설정
DEV_SERVER="dev-server"  # 개발 서버 주소
DEV_DB_NAME="template_dev_db"
LOCAL_DB_NAME="template_db"
DB_USER="postgres"
BACKUP_DIR="backups"
TEMP_DUMP="/tmp/dev_dump_$(date +%Y%m%d_%H%M%S).sql"

echo -e "${YELLOW}=========================================${NC}"
echo -e "${YELLOW}개발 서버 데이터 동기화${NC}"
echo -e "${YELLOW}=========================================${NC}"
echo ""

# 확인
echo -e "${RED}⚠️  경고: 로컬 데이터베이스가 개발 서버 데이터로 덮어씌워집니다!${NC}"
read -p "계속하시겠습니까? [y/N] " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo -e "${YELLOW}취소되었습니다.${NC}"
    exit 1
fi

# 개발 서버에서 덤프
echo -e "${GREEN}1. 개발 서버에서 데이터 덤프 중...${NC}"
ssh ${DEV_SERVER} "pg_dump -U ${DB_USER} ${DEV_DB_NAME}" > ${TEMP_DUMP}

# 로컬 DB 백업 (안전장치)
echo -e "${GREEN}2. 로컬 DB 백업 중...${NC}"
make db-backup

# 로컬 DB에 적용
echo -e "${GREEN}3. 로컬 DB에 데이터 적용 중...${NC}"
docker exec -i template_postgres psql -U ${DB_USER} ${LOCAL_DB_NAME} < ${TEMP_DUMP}

# 임시 파일 삭제
rm ${TEMP_DUMP}

echo ""
echo -e "${GREEN}✅ 동기화 완료!${NC}"
echo -e "${YELLOW}백업 파일은 ${BACKUP_DIR}/ 디렉토리에 저장되었습니다.${NC}"

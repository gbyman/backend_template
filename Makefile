.PHONY: help start stop restart logs db-reset db-backup db-restore clean build run run-dev test

# 기본 설정
DOCKER_COMPOSE := docker-compose
GRADLE := ./gradlew
POSTGRES_CONTAINER := template_postgres
DB_NAME := template_db
DB_USER := postgres
BACKUP_DIR := backups

help:
	@echo "========================================="
	@echo "Backend Template - 개발 환경 관리"
	@echo "========================================="
	@echo ""
	@echo "📦 Docker 관리:"
	@echo "  make start       - Docker 컨테이너 시작"
	@echo "  make stop        - Docker 컨테이너 중지"
	@echo "  make restart     - Docker 컨테이너 재시작"
	@echo "  make logs        - Docker 로그 확인"
	@echo ""
	@echo "🗄️  데이터베이스 관리:"
	@echo "  make db-reset    - DB 초기화 (데이터 삭제)"
	@echo "  make db-backup   - DB 백업"
	@echo "  make db-restore  - DB 복원"
	@echo "  make db-connect  - DB 접속 (psql)"
	@echo ""
	@echo "🔨 빌드 & 실행:"
	@echo "  make clean       - 빌드 파일 정리"
	@echo "  make build       - 프로젝트 빌드"
	@echo "  make run         - 애플리케이션 실행 (로컬)"
	@echo "  make run-dev     - 애플리케이션 실행 (개발)"
	@echo "  make test        - 테스트 실행"
	@echo ""
	@echo "🚀 통합 명령:"
	@echo "  make dev         - Docker 시작 + 앱 실행"
	@echo "  make all         - 전체 빌드 및 실행"
	@echo ""

# Docker 관리
start:
	@echo "🚀 Starting Docker containers..."
	$(DOCKER_COMPOSE) up -d
	@echo "⏳ Waiting for services to be ready..."
	@sleep 5
	@echo "✅ All services are running!"
	@echo ""
	@echo "📊 Service URLs:"
	@echo "  PostgreSQL: localhost:5432"
	@echo "  Redis:      localhost:6379"
	@echo "  pgAdmin:    http://localhost:5050"
	@echo ""

stop:
	@echo "🛑 Stopping Docker containers..."
	$(DOCKER_COMPOSE) down
	@echo "✅ All containers stopped!"

restart:
	@echo "🔄 Restarting Docker containers..."
	$(DOCKER_COMPOSE) restart
	@echo "✅ Containers restarted!"

logs:
	@echo "📋 Showing Docker logs (Ctrl+C to exit)..."
	$(DOCKER_COMPOSE) logs -f

# 데이터베이스 관리
db-reset:
	@echo "⚠️  WARNING: This will delete all local data!"
	@echo "Press Ctrl+C to cancel, or Enter to continue..."
	@read dummy
	@echo "🗑️  Removing containers and volumes..."
	$(DOCKER_COMPOSE) down -v
	@echo "🚀 Starting fresh containers..."
	$(DOCKER_COMPOSE) up -d
	@echo "⏳ Waiting for database to initialize..."
	@sleep 10
	@echo "✅ Database reset complete!"

db-backup:
	@echo "💾 Backing up local database..."
	@mkdir -p $(BACKUP_DIR)
	@docker exec $(POSTGRES_CONTAINER) pg_dump -U $(DB_USER) $(DB_NAME) > $(BACKUP_DIR)/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup created in $(BACKUP_DIR)/"
	@ls -lh $(BACKUP_DIR) | tail -1

db-restore:
	@echo "📂 Available backups:"
	@ls -1 $(BACKUP_DIR)/*.sql 2>/dev/null || echo "No backups found"
	@echo ""
	@read -p "Enter backup filename: " filename; \
	if [ -f "$(BACKUP_DIR)/$$filename" ]; then \
		echo "📥 Restoring database from $$filename..."; \
		docker exec -i $(POSTGRES_CONTAINER) psql -U $(DB_USER) $(DB_NAME) < $(BACKUP_DIR)/$$filename; \
		echo "✅ Database restored!"; \
	else \
		echo "❌ File not found: $$filename"; \
	fi

db-connect:
	@echo "🔗 Connecting to PostgreSQL..."
	@docker exec -it $(POSTGRES_CONTAINER) psql -U $(DB_USER) $(DB_NAME)

# 빌드 & 실행
clean:
	@echo "🧹 Cleaning build files..."
	$(GRADLE) clean
	@echo "✅ Clean complete!"

build:
	@echo "🔨 Building project..."
	$(GRADLE) clean build -x test
	@echo "✅ Build complete!"

build-with-test:
	@echo "🔨 Building project with tests..."
	$(GRADLE) clean build
	@echo "✅ Build complete with tests!"

run:
	@echo "🚀 Running application (local profile)..."
	$(GRADLE) :module-api:bootRun

run-dev:
	@echo "🚀 Running application (dev profile)..."
	$(GRADLE) :module-api:bootRun --args='--spring.profiles.active=dev'

test:
	@echo "🧪 Running tests..."
	$(GRADLE) test
	@echo "✅ Tests complete!"

# 통합 명령
dev: start
	@echo "⏳ Waiting for services..."
	@sleep 5
	@echo "🚀 Starting application..."
	$(MAKE) run

all: clean build run

# 도움말 별칭
h: help

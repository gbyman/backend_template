# ============================================
# Stage 1: Build
# ============================================
FROM gradle:8.13-jdk21-alpine AS builder

WORKDIR /app

# 의존성 캐싱 레이어 (소스 변경 시 의존성 재다운로드 방지)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY module-common/build.gradle module-common/build.gradle
COPY module-api/build.gradle module-api/build.gradle
COPY module-batch/build.gradle module-batch/build.gradle

RUN gradle dependencies --no-daemon || true

# 전체 소스 복사 및 빌드
COPY . .
RUN gradle :module-batch:bootJar -x test -Pprofile=prod --no-daemon

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:21-jre-alpine

# 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone && \
    apk del tzdata

# non-root 사용자 생성
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/module-batch/build/libs/*.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

# CI/CD 환경변수 설정 가이드

## 개요

backend_template은 **두 가지 방식**의 민감정보 관리를 지원합니다:

1. **환경변수 방식** (권장) - GitHub/GitLab Secrets
2. **Jasypt 암호화 방식** - 설정 파일 암호화

---

## 방식 1: 환경변수 (GitHub/GitLab Secrets)

### GitHub Actions 설정

#### 1. Repository Secrets 등록

`Settings` > `Secrets and variables` > `Actions` > `New repository secret`

**필수 환경변수:**
```
DB_URL=jdbc:postgresql://prod-db.example.com:5432/prod_db
DB_USERNAME=app_user
DB_PASSWORD=super-secret-password-123
REDIS_HOST=prod-redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=redis-secret-password-456
JWT_SECRET=eW91ci1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tYXQ...
```

**선택 환경변수:**
```
REDIS_SSL_ENABLED=true
AWS_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=mail-password-789
```

#### 2. Environment Secrets (환경별 설정)

`Settings` > `Environments` > `New environment`

**Environment 생성:**
- `development` (개발 서버)
- `staging` (스테이징 서버)
- `production` (운영 서버)

각 환경별로 다른 Secret 값 설정 가능

#### 3. GitHub Actions Workflow 예시

`.github/workflows/deploy.yml`:
```yaml
name: Deploy to Production

on:
  push:
    branches:
      - main

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production  # production 환경 Secret 사용

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build with Gradle
        run: ./gradlew clean build -x test

      - name: Build Docker image
        run: |
          docker build -t backend-template:latest .

      - name: Run Docker container
        run: |
          docker run -d \
            -e SPRING_PROFILES_ACTIVE=prod \
            -e DB_URL=${{ secrets.DB_URL }} \
            -e DB_USERNAME=${{ secrets.DB_USERNAME }} \
            -e DB_PASSWORD=${{ secrets.DB_PASSWORD }} \
            -e REDIS_HOST=${{ secrets.REDIS_HOST }} \
            -e REDIS_PASSWORD=${{ secrets.REDIS_PASSWORD }} \
            -e JWT_SECRET=${{ secrets.JWT_SECRET }} \
            -p 8080:8080 \
            backend-template:latest
```

---

### GitLab CI/CD 설정

#### 1. CI/CD Variables 등록

`Settings` > `CI/CD` > `Variables` > `Add variable`

**필수 변수:**
```
Key: DB_PASSWORD
Value: super-secret-password-123
Type: Variable
Environment scope: production
Flags: ✅ Protect variable, ✅ Mask variable
```

동일하게 설정:
- `DB_URL`
- `DB_USERNAME`
- `REDIS_HOST`
- `REDIS_PASSWORD`
- `JWT_SECRET`

#### 2. Environment Scope 설정

환경별로 다른 값 설정:
```
DB_PASSWORD (Environment: development) = dev-password
DB_PASSWORD (Environment: staging) = staging-password
DB_PASSWORD (Environment: production) = prod-password
```

#### 3. GitLab CI/CD Pipeline 예시

`.gitlab-ci.yml`:
```yaml
stages:
  - build
  - deploy

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

build:
  stage: build
  image: gradle:8.5-jdk21
  script:
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - module-api/build/libs/*.jar
    expire_in: 1 hour

deploy:production:
  stage: deploy
  image: docker:latest
  services:
    - docker:dind
  environment:
    name: production
  only:
    - main
  script:
    - docker build -t backend-template:latest .
    - |
      docker run -d \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e DB_URL=${DB_URL} \
        -e DB_USERNAME=${DB_USERNAME} \
        -e DB_PASSWORD=${DB_PASSWORD} \
        -e REDIS_HOST=${REDIS_HOST} \
        -e REDIS_PASSWORD=${REDIS_PASSWORD} \
        -e JWT_SECRET=${JWT_SECRET} \
        -p 8080:8080 \
        backend-template:latest
```

---

## 방식 2: Jasypt 암호화

환경변수 설정이 어려운 경우 사용 (자세한 내용은 `JASYPT_USAGE.md` 참고)

### 1. 값 암호화
```bash
java -cp ... app.backend.core.utils.JasyptEncryptUtil "secret-key" "postgres"
# 출력: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)
```

### 2. application.yml 적용
```yaml
spring:
  datasource:
    password: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)
```

### 3. 암호화 키만 환경변수로 전달
```yaml
# GitHub Actions
env:
  JASYPT_ENCRYPTOR_PASSWORD: ${{ secrets.JASYPT_SECRET_KEY }}

# GitLab CI
variables:
  JASYPT_ENCRYPTOR_PASSWORD: ${JASYPT_SECRET_KEY}
```

---

## 혼합 사용 (권장)

로컬 개발과 CI/CD 환경을 분리:

### application-local.yml (로컬 개발)
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:postgres}  # 기본값 사용
  data:
    redis:
      password: ${REDIS_PASSWORD:}  # 비밀번호 없음
```

### application-prod.yml (운영 환경)
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}  # 환경변수 필수
    # 또는
    password: ENC(암호화된값)  # Jasypt 사용
```

---

## Docker Compose 예시

### docker-compose.yml
```yaml
version: '3.8'

services:
  backend:
    image: backend-template:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=${REDIS_HOST}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "8080:8080"
    networks:
      - backend-network

networks:
  backend-network:
    driver: bridge
```

### .env (gitignore 필수!)
```bash
DB_URL=jdbc:postgresql://postgres:5432/template_db
DB_USERNAME=postgres
DB_PASSWORD=super-secret-password
REDIS_HOST=redis
REDIS_PASSWORD=redis-secret
JWT_SECRET=your-jwt-secret-key-base64-encoded
```

**실행:**
```bash
docker-compose --env-file .env up -d
```

---

## Kubernetes ConfigMap/Secret

### Secret 생성
```bash
kubectl create secret generic backend-secrets \
  --from-literal=DB_PASSWORD=super-secret-password \
  --from-literal=REDIS_PASSWORD=redis-secret \
  --from-literal=JWT_SECRET=jwt-secret-key
```

### Deployment 예시
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-template
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: backend
        image: backend-template:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          value: "jdbc:postgresql://postgres-service:5432/prod_db"
        - name: DB_USERNAME
          value: "app_user"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: backend-secrets
              key: DB_PASSWORD
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: backend-secrets
              key: REDIS_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: backend-secrets
              key: JWT_SECRET
```

---

## 환경변수 우선순위

Spring Boot는 다음 순서로 환경변수를 읽습니다:

1. **시스템 환경변수** (가장 높음)
2. **Java 시스템 속성** (`-D` 옵션)
3. **application-{profile}.yml**
4. **application.yml** (가장 낮음)

따라서 CI/CD에서 설정한 환경변수가 항상 우선됩니다!

---

## 보안 체크리스트

### ✅ 필수 확인사항

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는가?
- [ ] GitHub/GitLab Secrets가 Masked/Protected로 설정되어 있는가?
- [ ] 운영 환경 비밀번호가 충분히 강력한가? (최소 16자, 특수문자 포함)
- [ ] JWT Secret이 최소 512비트 (64바이트) 이상인가?
- [ ] 로그에 민감정보가 출력되지 않는가?
- [ ] 환경별로 다른 비밀번호를 사용하는가?
- [ ] 정기적으로 비밀번호를 변경하는가?

### ❌ 절대 하지 말 것

- ❌ 비밀번호를 소스코드에 하드코딩
- ❌ `.env` 파일을 Git에 커밋
- ❌ 운영 환경 비밀번호를 로컬에서 사용
- ❌ Slack, 이메일 등에 비밀번호 공유
- ❌ 로그 파일에 비밀번호 출력

---

## 참고 문서

- [GitHub Encrypted Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [GitLab CI/CD Variables](https://docs.gitlab.com/ee/ci/variables/)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Jasypt 사용 가이드](./JASYPT_USAGE.md)

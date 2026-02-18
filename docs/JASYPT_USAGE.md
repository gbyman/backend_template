# Jasypt 암호화 설정 가이드

## 개요

Jasypt(Java Simplified Encryption)를 사용하여 application.yml의 민감한 정보(DB 비밀번호, API 키 등)를 암호화합니다.

## 1. 라이브러리

```gradle
// module-common/build.gradle
api 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'
```

## 2. 암호화할 값 생성

### 방법 1: JasyptEncryptUtil 사용

```bash
# 컴파일
./gradlew :module-api:compileJava

# 암호화 실행
java -cp module-api/build/classes/java/main:$(./gradlew :module-common:dependencies --configuration runtimeClasspath | grep -oP '(?<=\-\-\- ).*\.jar' | tr '\n' ':') \
  app.backend.core.utils.JasyptEncryptUtil "your-secret-key" "postgres"
```

**출력 예시:**
```
=================================
Jasypt Encryption Utility
=================================
Secret Key: your-secret-key
Plain Text: postgres
---------------------------------
Encrypted: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)
Decrypted: postgres
=================================

Add this to your application.yml:
  password: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)

Run with:
  --jasypt.encryptor.password=your-secret-key
  or set environment variable:
  export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
```

### 방법 2: 온라인 도구 (비권장)

보안상 로컬에서 암호화하는 것을 권장합니다.

## 3. application.yml에 적용

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/template_db
    username: postgres
    password: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)  # 암호화된 값

  data:
    redis:
      password: ENC(aB3cD4eF5gH6iJ7kL8mN9oP0qR1sT2u)  # 암호화된 값

jwt:
  secret: ENC(yZ9xW8vU7tS6rQ5pO4nM3lK2jH1gF0e)  # 암호화된 값
```

## 4. 애플리케이션 실행

### 로컬 개발 환경

**방법 1: 환경변수 사용 (권장)**
```bash
export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
./gradlew :module-api:bootRun
```

**방법 2: 실행 인자 사용**
```bash
./gradlew :module-api:bootRun --args='--jasypt.encryptor.password=your-secret-key'
```

### 운영 환경

**방법 1: 환경변수 (권장)**
```bash
export JASYPT_ENCRYPTOR_PASSWORD=your-production-secret-key
java -jar module-api/build/libs/module-api-0.0.1-SNAPSHOT.jar
```

**방법 2: 실행 시 전달**
```bash
java -jar module-api/build/libs/module-api-0.0.1-SNAPSHOT.jar \
  --jasypt.encryptor.password=your-production-secret-key
```

**방법 3: Docker 환경변수**
```yaml
# docker-compose.yml
services:
  backend:
    image: backend-template:latest
    environment:
      - JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_SECRET_KEY}
```

## 5. 보안 주의사항

### ✅ 권장 사항

1. **암호화 키 분리**: 암호화 키는 절대 소스코드에 포함하지 않음
2. **환경별 키 관리**:
   - 로컬: `.env` 파일 (gitignore 필수)
   - 개발/운영: AWS Secrets Manager, HashiCorp Vault 등
3. **키 로테이션**: 주기적으로 암호화 키 변경
4. **암호화 값 검증**: 애플리케이션 시작 시 복호화 테스트

### ❌ 주의사항

1. **암호화 키를 Git에 커밋하지 마세요**
2. **로그에 암호화 키가 출력되지 않도록 주의**
3. **운영 환경에서는 반드시 강력한 키 사용**
4. **개발/운영 환경 키를 다르게 설정**

## 6. 암호화 대상

### 반드시 암호화해야 할 값

- ✅ 데이터베이스 비밀번호
- ✅ Redis 비밀번호
- ✅ JWT Secret Key
- ✅ 외부 API Key/Secret
- ✅ AWS Access Key/Secret Key
- ✅ 이메일 비밀번호
- ✅ 암호화/복호화 키

### 암호화 불필요한 값

- ❌ 데이터베이스 URL
- ❌ 데이터베이스 사용자명
- ❌ 서버 포트 번호
- ❌ 로그 레벨
- ❌ 공개 엔드포인트 경로

## 7. 트러블슈팅

### 암호화 키가 틀린 경우

```
org.jasypt.exceptions.EncryptionOperationNotPossibleException: Encryption raised an exception. A wrong password might have been provided
```

**해결방법:**
- 환경변수 `JASYPT_ENCRYPTOR_PASSWORD` 확인
- 암호화 시 사용한 키와 실행 시 키가 동일한지 확인

### 복호화 실패

```
Caused by: org.jasypt.exceptions.EncryptionOperationNotPossibleException
```

**해결방법:**
- 암호화된 값이 올바른지 확인
- `ENC()` 형식이 정확한지 확인
- 암호화 알고리즘 설정 확인 (JasyptConfig와 동일해야 함)

## 8. 예제

### 전체 예시

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db.example.com:5432/prod_db
    username: app_user
    password: ENC(fR3dD9sA2pL8mN5kH4jG7vB1cX6zQ0w)

  data:
    redis:
      host: prod-redis.example.com
      password: ENC(tY8uI7oP6aS5dF4gH3jK2lZ1xC0vB9n)

jwt:
  secret: ENC(mN5kL4jH3gF2dS1aQ0pO9iU8yT7rE6wV)

# AWS 설정
storage:
  s3:
    access-key: ENC(qW2eR3tY4uI5oP6aS7dF8gH9jK0lZ1x)
    secret-key: ENC(cV2bN3mM4kJ5hG6fD7sA8pL9oI0uY1t)
```

**실행:**
```bash
export JASYPT_ENCRYPTOR_PASSWORD=super-secret-production-key-2024
java -jar backend.jar --spring.profiles.active=prod
```

## 9. 암호화 알고리즘 변경 (선택)

더 강력한 암호화가 필요한 경우:

```java
// JasyptConfig.java
config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
config.setKeyObtentionIterations("10000");
```

**주의:** 알고리즘 변경 시 기존 암호화된 값을 모두 다시 암호화해야 합니다.

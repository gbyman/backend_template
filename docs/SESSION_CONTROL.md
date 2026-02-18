# 세션 동시 접속 제한 (중복 로그인 방지)

> JWT + Redis 기반 중복 로그인 방지 기능 가이드

## 📋 목차
- [개요](#개요)
- [왜 필요한가](#왜-필요한가)
- [작동 방식](#작동-방식)
- [설정 방법](#설정-방법)
- [사용 예제](#사용-예제)
- [주의사항](#주의사항)

---

## 개요

이 기능은 **하나의 계정으로 동시에 여러 곳에서 로그인하는 것을 방지**합니다.

### 기본 동작
- ✅ 마지막 로그인만 유효 (이전 세션 자동 만료)
- ✅ 특정 ID는 중복 로그인 허용 가능
- ✅ Redis 기반으로 분산 환경 지원

### 전제 조건
- **Redis 사용 모드** 활성화 필요: `jwt.use-redis: true`
- Redis 미사용 시 (stateless 모드)는 중복 로그인 체크 안함

---

## 왜 필요한가?

### 문제 상황

**시나리오 1: 계정 도용**
```
1. 사용자 A가 회사에서 로그인
2. 공격자가 A의 계정으로 집에서 로그인
3. 사용자 A와 공격자가 동시에 사용 중 ❌
```

**시나리오 2: 계정 공유**
```
1. 직원 A가 자신의 계정을 직원 B에게 알려줌
2. A와 B가 동시에 하나의 계정으로 작업 ❌
3. 감사 추적 불가능
```

### 해결 방법

✅ **중복 로그인 방지 활성화**
```
1. 사용자 A가 회사에서 로그인 (세션 1)
2. 공격자가 집에서 로그인 (세션 2 생성)
3. 세션 1 자동 만료 → A가 다시 로그인해야 함
4. A가 이상 징후를 인지하고 비밀번호 변경 ✅
```

---

## 작동 방식

### 1. 로그인 시

```
[사용자 A 로그인]
         ↓
[JWT 생성: AccessToken, RefreshToken]
         ↓
[Redis 저장]
  Key: userId (예: "user@example.com")
  Value: RefreshToken
  TTL: 7일
         ↓
[쿠키에 RefreshToken 저장]
```

### 2. 다른 곳에서 동일 계정 로그인 시

```
[사용자 A가 다른 장소에서 로그인]
         ↓
[새로운 JWT 생성]
         ↓
[Redis 업데이트]
  Key: userId (동일)
  Value: 새 RefreshToken (기존 값 덮어씀)
         ↓
[기존 세션의 RefreshToken ≠ Redis의 RefreshToken]
         ↓
[기존 세션에서 API 요청 시]
         ↓
[JwtService.isRefreshTokenMatched() = false]
         ↓
[401 Unauthorized 응답] ❌
```

### 3. 예외 처리 (특정 ID는 중복 로그인 허용)

```
[allow-duplicate.ids에 등록된 ID]
         ↓
[isRefreshTokenMatched() → 즉시 true 반환]
         ↓
[중복 로그인 허용] ✅
```

---

## 설정 방법

### 1. Redis 사용 활성화

**application.yml**
```yaml
jwt:
  use-redis: true  # ← Redis 기반 중복 로그인 방지 활성화
  access-expiration-duration: 30m
  refresh-expiration-duration: 7d
  refresh-cookie-secure: false
  secret: ${JWT_SECRET}
```

### 2. 중복 로그인 허용 ID 설정

**application.yml**
```yaml
# 중복 로그인 허용 ID 목록
allow-duplicate:
  ids:
    admin                    # 관리자 계정
    service-account         # 서비스 계정
    test@example.com        # 테스트 계정
```

**여러 줄로 작성 (가독성 좋음)**
```yaml
allow-duplicate:
  ids:
    - admin
    - service-account
    - test@example.com
```

**쉼표로 구분 (한 줄)**
```yaml
allow-duplicate:
  ids: admin, service-account, test@example.com
```

### 3. Redis 연결 설정

**application.yml**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
```

---

## 사용 예제

### 예제 1: 일반 사용자 (중복 로그인 차단)

**시나리오**
1. 사용자 `user@example.com`이 PC에서 로그인
2. 같은 사용자가 모바일에서 로그인
3. PC 세션 자동 만료

**테스트**
```bash
# 1. PC에서 로그인
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}'

# 응답: AccessToken, RefreshToken (쿠키)

# 2. 모바일에서 동일 계정 로그인
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password123"}'

# 3. PC에서 API 요청 (기존 세션)
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer {PC의 AccessToken}" \
  -b "templateRefresh={PC의 RefreshToken}"

# 응답: 401 Unauthorized ❌
# 메시지: "다른 곳에서 로그인되었습니다. 다시 로그인해주세요."
```

---

### 예제 2: 관리자 계정 (중복 로그인 허용)

**설정**
```yaml
allow-duplicate:
  ids:
    - admin
```

**테스트**
```bash
# 1. 관리자가 PC에서 로그인
curl -X POST http://localhost:8080/api/login \
  -d '{"username":"admin","password":"admin123"}'

# 2. 관리자가 모바일에서 로그인
curl -X POST http://localhost:8080/api/login \
  -d '{"username":"admin","password":"admin123"}'

# 3. PC에서 API 요청 (기존 세션)
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer {PC의 AccessToken}" \
  -b "templateRefresh={PC의 RefreshToken}"

# 응답: 200 OK ✅
# PC와 모바일 세션 둘 다 유효
```

---

## 구현 상세

### JwtService.isRefreshTokenMatched()

**코드 위치**: `JwtService.java:217-239`

```java
public boolean isRefreshTokenMatched() {
    // 1. Redis 미사용 시 중복 로그인 체크 안함
    if (!useRedis) {
        return true;
    }

    // 2. local 환경은 체크 안함
    if (environment.matchesProfiles("local")) {
        return true;
    }

    String refreshToken = getRequestRefreshToken();
    String userId = getUserId(refreshToken);

    // 3. 중복 로그인 허용 ID 체크
    if (allowDuplicateLoginIds.contains(userId)) {
        return true; // ← 예외 처리
    }

    // 4. Redis의 RefreshToken과 클라이언트의 RefreshToken 비교
    Optional<RefreshTokenEntity> refreshTokenOpt =
        refreshTokenRepository.findById(userId);

    return refreshTokenOpt.get()
        .getRefreshToken()
        .equals(refreshToken); // ← 불일치 시 false → 401 에러
}
```

---

## 주의사항

### 1. Redis 필수

이 기능은 **Redis 기반**으로 작동합니다.

```yaml
jwt:
  use-redis: false  # ← false면 중복 로그인 체크 안함
```

### 2. local 환경 예외

**local 프로파일**에서는 자동으로 중복 로그인 체크를 건너뜁니다.

```java
if (environment.matchesProfiles("local")) return true;
```

**이유**: 개발 중 불편함 방지

**운영 환경 적용**:
```yaml
spring:
  profiles:
    active: prod  # ← local이 아닌 환경에서만 체크 활성화
```

### 3. 쿠키 보안

RefreshToken은 **HttpOnly 쿠키**로 저장됩니다.

```yaml
jwt:
  refresh-cookie-secure: true  # ← HTTPS에서만 쿠키 전송 (운영 환경)
```

**local/dev 환경**:
```yaml
jwt:
  refresh-cookie-secure: false  # ← HTTP에서도 쿠키 전송 허용
```

### 4. 예외 ID 관리

`allow-duplicate.ids`에 등록할 때 **신중하게** 결정하세요.

**허용 기준**:
- ✅ 관리자 계정 (여러 관리자가 동시 작업 필요)
- ✅ 서비스 계정 (API 통신용)
- ✅ 테스트 계정 (QA 팀)
- ❌ 일반 사용자 계정 (보안 위험)

---

## 트러블슈팅

### Q1. 로그인은 되는데 API 요청 시 401 에러

**원인**: RefreshToken 불일치

**확인**:
```bash
# Redis에서 RefreshToken 확인
redis-cli
> GET refreshToken:user@example.com
> TTL refreshToken:user@example.com
```

**해결**: 다시 로그인

---

### Q2. 중복 로그인이 허용되지 않음

**확인 1**: `allow-duplicate.ids` 설정 확인
```yaml
allow-duplicate:
  ids:
    - admin  # ← 들여쓰기 확인
```

**확인 2**: Redis 사용 확인
```yaml
jwt:
  use-redis: true  # ← true 확인
```

**확인 3**: 로그 확인
```
DEBUG app.backend.core.jwt.service.JwtService -
  중복 로그인 허용 ID: [admin, service-account]
```

---

### Q3. local 환경에서도 체크하고 싶음

**JwtService.java 수정**:
```java
public boolean isRefreshTokenMatched() {
    if (!useRedis) return true;

    // 이 줄 제거 또는 주석 처리
    // if (environment.matchesProfiles("local")) return true;

    ...
}
```

---

## 관련 문서

- [JWT 인증 가이드](./JWT_AUTHENTICATION.md) (작성 예정)
- [Redis 설정 가이드](./REDIS_SETUP.md) (작성 예정)
- [프로젝트 README](../README.md)

---

**작성일**: 2025-02-15
**최종 수정**: 2025-02-15

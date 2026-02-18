# 로그 마스킹 가이드

> 로그에 출력되는 민감 정보를 자동으로 마스킹 처리하는 기능입니다.

## 📋 목차
- [개요](#개요)
- [왜 필요한가](#왜-필요한가)
- [작동 방식](#작동-방식)
- [마스킹 패턴 목록](#마스킹-패턴-목록)
- [사용 예제](#사용-예제)
- [커스텀 패턴 추가](#커스텀-패턴-추가)
- [주의사항](#주의사항)

---

## 개요

Logback 마스킹 패턴은 로그 출력 시점에 민감 정보를 자동으로 감지하고 마스킹 처리합니다.

### 적용된 패턴 (4가지)
1. **전화번호** - `010-1234-5678` → `010-****-5678`
2. **이메일** - `user@example.com` → `u***@example.com`
3. **비밀번호 (JSON)** - `"password":"secret"` → `"password":"*****"`
4. **비밀번호 (텍스트)** - `password=secret` → `password=*****`

### 적용 범위
- ✅ 콘솔 로그 (CONSOLE Appender)
- ✅ 파일 로그 (FILE Appender)
- ✅ 에러 로그 (ERROR_FILE Appender)
- ⚠️ JSON 로그 (ELK Stack)는 별도 설정 필요

### 제외된 패턴
주민번호, 카드번호, 계좌번호, JWT 토큰, API Key 등은 **로그 출력 자체를 금지**하는 것이 바람직하므로 마스킹 패턴에서 제외했습니다. 이런 정보는 디버깅 시에도 로그로 확인할 필요가 없습니다.

---

## 왜 필요한가?

### 문제 상황

```java
// 개발자가 실수로 민감 정보를 로그에 출력
log.info("사용자 정보: {}", user);
// 출력: 사용자 정보: User(name=홍길동, phone=010-1234-5678, ssn=123456-1234567)

log.debug("로그인 요청: {}", loginRequest);
// 출력: 로그인 요청: {username=user@example.com, password=myPassword123}

log.error("결제 실패: {}", paymentInfo);
// 출력: 결제 실패: {cardNumber=1234-5678-9012-3456, cvv=123}
```

### 위험성

1. **개인정보보호법 위반**
   - 로그 파일에 주민번호, 전화번호 등 평문 저장
   - 과태료 최대 5천만원

2. **보안 감사 실패**
   - 정보보안 인증 (ISMS-P) 부적합
   - 금융권, 공공기관 프로젝트 탈락

3. **정보 유출 위험**
   - 로그 파일 접근만으로 민감 정보 노출
   - 비밀번호, 토큰 등 탈취 가능

---

## 작동 방식

### 1. 로그 흐름

```
[코드] log.info("phone=010-1234-5678")
         ↓
[Logback] MaskingPatternLayout 적용
         ↓
[정규식] 전화번호 패턴 매칭
         ↓
[마스킹] phone=010-****-5678
         ↓
[출력] 파일/콘솔에 기록
```

### 2. 설정 위치

**logback-spring.xml**
```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="app.backend.core.log.MaskingPatternLayout">
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </layout>
    </encoder>
</appender>
```

---

## 마스킹 패턴 목록

### 1. 전화번호 마스킹 📱
```
원본: 010-1234-5678
마스킹: 010-****-5678

원본: 01012345678
마스킹: 010-****-5678

원본: 02-123-4567
마스킹: 02-****-4567
```

**정규식**: `(\d{2,3})-?(\d{3,4})-?(\d{4})`

---

### 2. 이메일 마스킹 📧
```
원본: user@example.com
마스킹: u***@example.com

원본: hong.gildong@company.co.kr
마스킹: h***@company.co.kr
```

**정규식**: `([a-zA-Z0-9._-])[a-zA-Z0-9._-]*@([a-zA-Z0-9.-]+)`

---

### 3. 비밀번호 마스킹 (JSON) 🔒
```
원본: {"password":"myPassword123"}
마스킹: {"password":"*****"}

원본: "password": "secret123"
마스킹: "password": "*****"
```

**정규식**: `("password"\s*:\s*")[^"]*(")`

---

### 4. 비밀번호 마스킹 (텍스트) 🔐
```
원본: password=myPassword123
마스킹: password=*****

원본: pwd=secret, username=user
마스킹: pwd=*****, username=user
```

**정규식**: `(password\s*=\s*)[^\s,)]*`

---

## 사용 예제

### 예제 1: 사용자 정보 로깅

```java
@RestController
public class UserController {

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDto user) {
        // 원본 로그
        log.info("사용자 생성 요청: name={}, phone={}, email={}",
                 user.getName(), user.getPhone(), user.getEmail());

        // 실제 출력
        // 사용자 생성 요청: name=홍길동, phone=010-****-5678, email=h***@example.com

        return ResponseEntity.ok(user);
    }
}
```

### 예제 2: 로그인 처리

```java
@Service
public class AuthService {

    public TokenResponse login(LoginRequest request) {
        log.debug("로그인 시도: {}", request);
        // LoginRequest(username=user@example.com, password=myPassword123)

        // 실제 출력
        // 로그인 시도: LoginRequest(username=u***@example.com, password=*****)

        String token = generateToken(request);
        log.info("토큰 발급: {}", token);
        // 실제 출력
        // 토큰 발급: *****

        return new TokenResponse(token);
    }
}
```

### 예제 3: 에러 로깅

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        log.error("에러 발생: uri={}, method={}, error={}",
                  request.getRequestURI(),
                  request.getMethod(),
                  ex.getMessage());

        return ResponseEntity.status(500).build();
    }
}
```

---

## 커스텀 패턴 추가

### ⚠️ 중요 원칙

**민감 정보는 로그에 출력하지 않는 것이 최선입니다.**

마스킹은 개발자의 실수로 민감 정보가 출력되었을 때의 **안전장치**이지, 민감 정보를 로그에 출력하는 것을 **정당화하는 수단이 아닙니다**.

- ✅ 전화번호, 이메일 - 디버깅 시 필요할 수 있으므로 마스킹
- ❌ 주민번호, 카드번호, 토큰 - 로그 출력 자체를 금지

### 방법 1: 코드로 추가 (필요한 경우)

```java
// MaskingPatternLayout.java에 패턴 추가

private void registerDefaultPatterns() {
    // 기존 패턴들...

    // 예시: IP 주소 마스킹 (필요한 경우만)
    // 192.168.1.100 → 192.168.***.***
    maskingPatterns.add(new MaskingPattern(
        "IP 주소",
        Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.)(\\d{1,3}\\.\\d{1,3})"),
        "$1***.***.***"
    ));
}
```

### 추가하면 안 되는 패턴

- ❌ 주민등록번호 - 로그에 출력하지 마세요
- ❌ 카드번호 - 로그에 출력하지 마세요
- ❌ 계좌번호 - 로그에 출력하지 마세요
- ❌ JWT 토큰 - 로그에 출력하지 마세요
- ❌ API Key - 로그에 출력하지 마세요

### 방법 2: 환경별 설정

```java
// application.yml에서 패턴 관리 (향후 확장)
logging:
  masking:
    patterns:
      - name: "사업자등록번호"
        regex: "(\d{3})-?(\d{2})-?(\d{5})"
        replacement: "$1-**-*****"
```

---

## 주의사항

### 1. 성능 고려

마스킹은 모든 로그에 정규식을 적용하므로 약간의 성능 오버헤드가 있습니다.

**권장사항**:
- 운영 환경에서는 로그 레벨을 INFO 이상으로 설정
- DEBUG 로그는 최소화
- 필요하지 않은 패턴은 제거

### 2. 완벽하지 않음

마스킹은 **최선의 방어**이지 **완벽한 방어**가 아닙니다.

**근본적인 해결책**:
```java
// ❌ 나쁜 예: 민감 정보를 직접 로깅
log.info("사용자: {}", user);

// ✅ 좋은 예: 필요한 정보만 로깅
log.info("사용자 ID: {}, 이름: {}", user.getId(), user.getName());

// ✅ 더 좋은 예: DTO에서 toString() 재정의
@Override
public String toString() {
    return String.format("User(id=%d, name=%s)", id, name);
    // phone, email 등은 제외
}
```

### 3. JSON 로그는 별도 처리

ELK Stack 사용 시 JSON 로그는 LogstashEncoder를 사용하므로 MaskingPatternLayout이 적용되지 않습니다.

**해결 방법**:
```java
// LogstashEncoder 커스터마이징 필요
// 또는 Elasticsearch Ingest Pipeline에서 마스킹 처리
```

### 4. 마스킹 테스트

```java
@Test
void testMasking() {
    String log = "전화번호: 010-1234-5678, 이메일: user@example.com";
    MaskingPatternLayout layout = new MaskingPatternLayout();

    String masked = layout.maskMessage(log);

    assertThat(masked).contains("010-****-5678");
    assertThat(masked).contains("u***@example.com");
}
```

---

## 기존 코드와 비교

### 기존: Jackson Serializer 마스킹 (API 응답용)

프로젝트에는 이미 `MaskingSerializer.java`가 있습니다:
```java
@JsonSerialize(using = MaskingSerializer.class)
@Masking(type = MaskingType.PHONE)
private String phoneNumber;
```

**적용 범위**: API 응답 JSON만

### 신규: Logback 마스킹 (로그용)

**적용 범위**: 모든 로그 출력 (콘솔, 파일)

### 결합 사용

```java
@Entity
public class User {
    // API 응답 마스킹
    @JsonSerialize(using = MaskingSerializer.class)
    @Masking(type = MaskingType.PHONE)
    private String phone;

    // 로그 출력 시 자동 마스킹 (별도 설정 불필요)
}

// API 응답: {"phone": "010-****-5678"}
// 로그 출력: User(phone=010-****-5678)
```

---

## 트러블슈팅

### Q1. 마스킹이 적용되지 않아요

**확인사항**:
1. logback-spring.xml에 MaskingPatternLayout 설정 확인
2. 클래스 경로가 정확한지 확인 (`app.backend.core.log.MaskingPatternLayout`)
3. 애플리케이션 재시작

### Q2. 특정 패턴만 제외하고 싶어요

```java
// MaskingPatternLayout.java
private void registerDefaultPatterns() {
    // 필요한 패턴만 등록
    maskingPatterns.add(new MaskingPattern(...)); // 전화번호만
    // maskingPatterns.add(...); // 이메일은 주석 처리
}
```

### Q3. ELK Stack에서는 어떻게 해요?

JSON 로그는 Elasticsearch Ingest Pipeline 사용:
```json
PUT _ingest/pipeline/masking-pipeline
{
  "processors": [
    {
      "gsub": {
        "field": "message",
        "pattern": "(\\d{2,3})-?(\\d{3,4})-?(\\d{4})",
        "replacement": "$1-****-$3"
      }
    }
  ]
}
```

---

## 참고 자료

- [Logback 공식 문서](https://logback.qos.ch/)
- [개인정보보호법 안전조치 기준](https://www.law.go.kr/)
- [ISMS-P 인증 기준](https://isms.kisa.or.kr/)
- [기존 마스킹 Serializer](../module-common/src/main/java/app/backend/core/serializer/MaskingSerializer.java)

---

**작성일**: 2025-02-15
**최종 수정**: 2025-02-15

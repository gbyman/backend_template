# Utility Classes Guide

모든 유틸리티 클래스는 `@UtilityClass` (Lombok)를 사용하여 static 메서드로 제공됩니다.

## 📋 목차

- [HtmlSanitizeUtils](#htmlsanitizeutils---html-필터링) - HTML 필터링 (XSS 방어)
- [EncryptionUtils](#encryptionutils---암호화) - 암호화/복호화
- [JasyptEncryptUtil](#jasyptencryptutil---jasypt-암호화) - Jasypt 설정 파일 암호화
- [ValidationUtils](#validationutils---입력값-검증) - 입력값 검증
- [StringUtils](#stringutils---문자열-처리) - 문자열 처리
- [MaskingUtils](#maskingutils---민감정보-마스킹) - 민감정보 마스킹
- [SecurityUtils](#securityutils---인증정보) - Spring Security 인증 정보
- [FileUtils](#fileutils---파일-처리) - 파일 처리
- [DateTimeUtils](#datetimeutils---날짜시간-변환) - 날짜/시간 변환
- [IpUtils](#iputils---ip-추출) - 클라이언트 IP 추출
- [JsonUtil](#jsonutil---json-처리) - JSON 처리
- [ServletUtils](#servletutils---http-요청응답) - HTTP 요청/응답
- [CookieUtils](#cookieutils---쿠키-관리) - 쿠키 관리
- [ReqContextUtils](#reqcontextutils---요청-컨텍스트) - 요청 컨텍스트
- [ResponseUtils](#responseutils---응답-생성) - 응답 생성
- [UuidUtils](#uuidutils---uuid-생성) - UUID 생성
- [BeanUtils](#beanutils---bean-조회) - Spring Bean 조회

---

## HtmlSanitizeUtils - HTML 필터링

WYSIWYG 에디터에서 받은 HTML을 필터링하여 XSS 공격을 방지합니다.

### 주요 메서드

```java
// 1. WYSIWYG 에디터 콘텐츠 (게시판, 블로그) - 가장 많이 사용 ⭐⭐⭐
String safe = HtmlSanitizeUtils.sanitizeForEditor(userHtml);
// 허용: h1-h6, p, br, strong, em, ul, ol, li, a, img, table, pre, code

// 2. 댓글/설명 (기본 포맷)
String safe = HtmlSanitizeUtils.sanitizeBasic(comment);
// 허용: b, em, i, strong, u, br, a

// 3. 짧은 텍스트 (프로필, 한 줄 소개)
String safe = HtmlSanitizeUtils.sanitizeSimple(bio);
// 허용: b, em, i, strong, u, br

// 4. 평문 변환 (검색 인덱싱, 미리보기)
String text = HtmlSanitizeUtils.toPlainText(html);
// 모든 HTML 태그 제거

// 5. 위험한 콘텐츠 감지
boolean isDangerous = HtmlSanitizeUtils.containsDangerousContent(html);
```

### 실전 사용

```java
@Service
public class PostService {
    public PostRespDto createPost(PostReqDto dto) {
        // DB 저장 전 HTML 필터링 (필수!)
        String safeContent = HtmlSanitizeUtils.sanitizeForEditor(dto.getContent());

        Post post = Post.builder()
            .title(dto.getTitle())
            .content(safeContent)  // 필터링된 HTML 저장
            .build();

        return postRepository.save(post);
    }

    public String getPreview(Long postId) {
        Post post = postRepository.findById(postId);
        // 미리보기용 평문 추출
        String plainText = HtmlSanitizeUtils.toPlainText(post.getContent());
        return plainText.substring(0, Math.min(100, plainText.length()));
    }
}
```

---

## EncryptionUtils - 암호화

AES-256 암호화, SHA-256 해싱, Base64 인코딩을 제공합니다.

### 주요 메서드

```java
// 1. AES-256 암호화/복호화 (개인정보 저장)
String secretKey = "my-secret-key-32-chars-long!!";
String encrypted = EncryptionUtils.encryptAES256("홍길동", secretKey);
String decrypted = EncryptionUtils.decryptAES256(encrypted, secretKey);

// 2. SHA-256 해싱 (파일 무결성, 데이터 검증)
String hash = EncryptionUtils.hashSHA256("important-data");
boolean valid = EncryptionUtils.verifySHA256("important-data", hash);

// 3. 비밀번호 해싱 (Salt 사용)
String salt = EncryptionUtils.generateSalt(16);
String hashedPw = EncryptionUtils.hashPasswordWithSalt(password, salt);
boolean valid = EncryptionUtils.verifyPassword(inputPw, hashedPw, salt);

// 4. Base64 인코딩/디코딩
String encoded = EncryptionUtils.encodeBase64("Hello");
String decoded = EncryptionUtils.decodeBase64ToString(encoded);
```

### 실전 사용

```java
@Service
public class UserService {
    private static final String ENCRYPTION_KEY = "your-32-char-secret-key-here!";

    public void saveUserInfo(UserDto dto) {
        // 주민번호 암호화 저장
        String encryptedRrn = EncryptionUtils.encryptAES256(
            dto.getResidentNumber(),
            ENCRYPTION_KEY
        );

        user.setResidentNumber(encryptedRrn);
        userRepository.save(user);
    }

    public String getResidentNumber(Long userId) {
        User user = userRepository.findById(userId);
        // 복호화
        return EncryptionUtils.decryptAES256(
            user.getResidentNumber(),
            ENCRYPTION_KEY
        );
    }
}
```

---

## JasyptEncryptUtil - Jasypt 암호화

application.yml 설정 파일의 민감 정보를 암호화하는 유틸리티입니다.

### 주요 메서드

```java
// 1. 평문 암호화
String encrypted = JasyptEncryptUtil.encrypt("your-secret-key", "postgres");
// 출력: ENC(encrypted-value)

// 2. 암호화된 값 복호화
String decrypted = JasyptEncryptUtil.decrypt("your-secret-key", encrypted);
```

### 실전 사용

**1. 명령줄에서 암호화:**
```bash
# Gradle 실행
./gradlew :module-common:compileJava

# 암호화 실행
java -cp module-common/build/classes/java/main:$(./gradlew :module-common:dependencies --configuration runtimeClasspath | grep -oP '(?<=\-\-\- ).*\.jar' | tr '\n' ':') \
  app.backend.core.utils.JasyptEncryptUtil "your-secret-key" "postgres"
```

**출력:**
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
```

**2. application.yml에 적용:**
```yaml
spring:
  datasource:
    password: ENC(XvZ8dF3nQp2mK5hL9wR1tY7uI4oP6aS)
```

**3. 애플리케이션 실행 시 암호화 키 전달:**
```bash
# 환경변수
export JASYPT_ENCRYPTOR_PASSWORD=your-secret-key
./gradlew :module-api:bootRun

# 또는 실행 인자
./gradlew :module-api:bootRun --args='--jasypt.encryptor.password=your-secret-key'
```

자세한 내용은 `docs/JASYPT_USAGE.md` 참고

---

## ValidationUtils - 입력값 검증

이메일, URL, 숫자, 영문숫자 검증을 제공합니다.

### 주요 메서드

```java
// 1. 이메일 검증
boolean valid = ValidationUtils.isValidEmail("user@example.com");  // true
ValidationUtils.requireValidEmail(email, "이메일");  // 실패 시 예외

// 2. URL 검증 (http/https만)
boolean valid = ValidationUtils.isValidUrl("https://example.com");  // true
ValidationUtils.requireValidUrl(url, "웹사이트");

// 3. 숫자만 검증
boolean valid = ValidationUtils.isNumeric("123456");  // true
ValidationUtils.requireNumeric(code, "인증번호");

// 4. 영문+숫자만 검증
boolean valid = ValidationUtils.isAlphanumeric("abc123");  // true
ValidationUtils.requireAlphanumeric(userId, "사용자 ID");
```

### 실전 사용

```java
@Service
public class AuthService {
    public void sendVerificationCode(String email, String phone) {
        // 이메일 또는 전화번호 중 하나는 필수
        if (!ValidationUtils.isValidEmail(email) &&
            !ValidationUtils.isNumeric(phone)) {
            throw new BizException("이메일 또는 전화번호가 유효하지 않습니다.");
        }

        String code = StringUtils.generateRandomNumeric(6);
        ValidationUtils.requireNumeric(code, "인증번호");  // 검증

        smsService.send(phone, "인증번호: " + code);
    }
}
```

---

## StringUtils - 문자열 처리

랜덤 문자열 생성, Case 변환, 마스킹 등을 제공합니다.

### 주요 메서드

```java
// 1. 랜덤 문자열 생성 (SecureRandom 사용)
String random = StringUtils.generateRandomAlphanumeric(10);  // "aB3xK9mP2q"
String code = StringUtils.generateRandomNumeric(6);          // "483726"
String token = StringUtils.generateRandomString(8);          // "aBxKmPqR"

// 2. Case 변환
String camel = StringUtils.toCamelCase("user_name");      // "userName"
String snake = StringUtils.toSnakeCase("userName");       // "user_name"
String kebab = StringUtils.toKebabCase("userName");       // "user-name"

// 3. 대문자 변환
String cap = StringUtils.capitalize("HELLO");             // "Hello"
String words = StringUtils.capitalizeWords("hello world"); // "Hello World"

// 4. 마스킹
String masked = StringUtils.mask("1234567890", 2, 2);     // "12******90"
```

### 실전 사용

```java
@Service
public class AuthService {
    public String sendVerificationCode(String email) {
        // 6자리 인증번호 생성
        String code = StringUtils.generateRandomNumeric(6);
        emailService.send(email, "인증번호: " + code);
        return code;
    }
}

@Service
public class PostService {
    public String createSlug(String title, Long id) {
        // URL용 slug 생성 (SEO)
        String slug = StringUtils.toKebabCase(title);
        return slug + "-" + id;
        // "Hello World" → "hello-world-123"
    }
}
```

---

## MaskingUtils - 민감정보 마스킹

개인정보를 마스킹하여 로그나 응답에 노출을 방지합니다.

### 주요 메서드

```java
// 1. IP 마스킹
String masked = MaskingUtils.maskIp("192.168.123.456");  // "192.168.***.456"

// 2. 이메일 마스킹
String masked = MaskingUtils.maskEmail("user@example.com");  // "us****@example.com"

// 3. 이름 마스킹
String masked = MaskingUtils.maskName("홍길동");  // "홍*동"

// 4. 전화번호 마스킹
String masked = MaskingUtils.maskPhone("010-1234-5678");  // "010-****-5678"

// 5. ID 마스킹
String masked = MaskingUtils.maskId("user1234");  // "us******"
```

### 실전 사용

```java
@Slf4j
@Service
public class UserService {
    public void processLogin(String email, String ip) {
        // 로그에 민감정보 마스킹 후 출력
        log.info("Login attempt: email={}, ip={}",
            MaskingUtils.maskEmail(email),
            MaskingUtils.maskIp(ip)
        );
    }

    public UserRespDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId);
        return UserRespDto.builder()
            .name(MaskingUtils.maskName(user.getName()))
            .phone(MaskingUtils.maskPhone(user.getPhone()))
            .email(MaskingUtils.maskEmail(user.getEmail()))
            .build();
    }
}
```

---

## SecurityUtils - 인증정보

Spring Security 인증 정보를 쉽게 조회합니다.

### 주요 메서드

```java
// 1. 현재 인증 객체
Authentication auth = SecurityUtils.getAuthentication();

// 2. 현재 사용자명
String username = SecurityUtils.getUsername();

// 3. UserDetails 조회
UserDetails user = SecurityUtils.getUserDetails();

// 4. 권한 확인
boolean hasAuth = SecurityUtils.hasAuthority("READ_POST");
boolean hasRole = SecurityUtils.hasRole("ADMIN");  // ROLE_ prefix 자동 추가
```

### 실전 사용

```java
@Service
public class PostService {
    public void createPost(PostReqDto dto) {
        // 현재 로그인한 사용자 ID 조회
        String currentUser = SecurityUtils.getUsername();

        Post post = Post.builder()
            .title(dto.getTitle())
            .content(dto.getContent())
            .author(currentUser)
            .build();

        postRepository.save(post);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId);

        // 관리자이거나 작성자인 경우만 삭제 가능
        if (!SecurityUtils.hasRole("ADMIN") &&
            !post.getAuthor().equals(SecurityUtils.getUsername())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
```

---

## FileUtils - 파일 처리

파일 확장자 추출, 랜덤 파일명 생성 등을 제공합니다.

### 주요 메서드

```java
// 1. 확장자 추출
Optional<String> ext = FileUtils.getFileExtension("image.jpg");  // "jpg"
Optional<String> ext = FileUtils.getFileExtension(multipartFile);

// 2. 확장자로 필터링
boolean hasExcel = FileUtils.containsFileWithExtension(files, "xlsx");
List<MultipartFile> images = FileUtils.filterFilesByExtension(files, "jpg");

// 3. 랜덤 파일명 생성
String fileName = FileUtils.createRandomFileName("jpg");  // "uuid.jpg"
String fileName = FileUtils.createRandomFileName(multipartFile);

// 4. MIME 타입 추측
String mimeType = FileUtils.getMimeTypeFromExtension("image.jpg");  // "image/jpeg"
```

### 실전 사용

```java
@Service
public class FileUploadService {
    public String uploadFile(MultipartFile file) {
        // 랜덤 파일명 생성 (중복 방지)
        String fileName = FileUtils.createRandomFileName(file);
        // uuid-random-string.jpg

        // S3 업로드
        s3Client.putObject(bucket, fileName, file.getInputStream());

        return fileName;
    }
}
```

---

## DateTimeUtils - 날짜/시간 변환

Epoch timestamp를 LocalDate/LocalDateTime으로 변환합니다.

### 주요 메서드

```java
// 1. Epoch 초 → LocalDate
LocalDate date = DateTimeUtils.toLocalDateFromEpochSeconds(1748240595.0);

// 2. Epoch 초 → LocalDateTime
LocalDateTime dt = DateTimeUtils.toLocalDateTimeFromEpochSeconds(1748240595.0);

// 3. Epoch 밀리초 → LocalDateTime
LocalDateTime dt = DateTimeUtils.toLocalDateTimeFromEpochMillis(1748240595000L);

// 4. 시간대 지정
LocalDate date = DateTimeUtils.toLocalDateFromEpochSeconds(
    1748240595.0,
    ZoneId.of("Asia/Seoul")
);
```

---

## IpUtils - IP 추출

프록시/로드밸런서를 고려한 실제 클라이언트 IP를 추출합니다.

### 주요 메서드

```java
String clientIp = IpUtils.getClientIp(request);
// X-Forwarded-For, X-Real-IP 헤더 확인 후 remoteAddr 사용
```

### 실전 사용

```java
@RestController
public class AuthController {
    public void login(HttpServletRequest request, LoginDto dto) {
        String ip = IpUtils.getClientIp(request);
        log.info("Login from IP: {}", MaskingUtils.maskIp(ip));
    }
}
```

---

## JsonUtil - JSON 처리

마크다운 코드 블록에서 JSON 추출 및 DTO 변환을 제공합니다.

### 주요 메서드

```java
// 1. JSON → DTO 변환
UserDto user = JsonUtil.convertDtoFromJson(jsonString, UserDto.class);

// 2. JSON → Generic 변환
List<UserDto> users = JsonUtil.convertDtoFromJson(
    jsonString,
    new TypeReference<List<UserDto>>() {}
);

// 3. 텍스트에서 JSON 추출
String json = JsonUtil.extractJsonFromText(markdownText);
// "```json\n{...}\n```" → "{...}"
```

---

## ServletUtils - HTTP 요청/응답

HttpServletRequest/Response 조회를 제공합니다.

### 주요 메서드

```java
HttpServletRequest request = ServletUtils.getRequest();
HttpServletResponse response = ServletUtils.getResponse();
```

---

## CookieUtils - 쿠키 관리

쿠키 생성, 조회, 삭제를 제공합니다.

### 주요 메서드

```java
// 1. 쿠키 생성
CookieUtils.addCookie(response, "refreshToken", token, 7 * 24 * 60 * 60);

// 2. 쿠키 조회
Optional<Cookie> cookie = CookieUtils.getCookie(request, "refreshToken");
String value = CookieUtils.getCookieValue(request, "refreshToken").orElse(null);

// 3. 쿠키 삭제
CookieUtils.deleteCookie(response, "refreshToken");
```

---

## ReqContextUtils - 요청 컨텍스트

요청 ID, 타임스탬프 등 요청 컨텍스트 정보를 조회합니다.

### 주요 메서드

```java
String requestId = ReqContextUtils.getRequestId();
Long timestamp = ReqContextUtils.getTimestamp();
```

---

## ResponseUtils - 응답 생성

HTTP 응답을 쉽게 생성합니다. (주로 Filter/Interceptor에서 사용)

### 주요 메서드

```java
ResponseUtils.sendJsonResponse(response, HttpStatus.OK, data);
ResponseUtils.sendErrorResponse(response, HttpStatus.BAD_REQUEST, "에러 메시지");
```

---

## UuidUtils - UUID 생성

UUID를 생성합니다.

### 주요 메서드

```java
String uuid = UuidUtils.generate();  // UUID v4
```

---

## BeanUtils - Spring Bean 조회

ApplicationContext에서 Bean을 조회합니다.

### 주요 메서드

```java
UserService service = BeanUtils.getBean(UserService.class);
Object bean = BeanUtils.getBean("beanName");
```

---

## 📝 베스트 프랙티스

### 1. HTML 콘텐츠는 반드시 필터링
```java
// ❌ 나쁜 예
post.setContent(dto.getContent());  // XSS 취약

// ✅ 좋은 예
String safeContent = HtmlSanitizeUtils.sanitizeForEditor(dto.getContent());
post.setContent(safeContent);
```

### 2. 민감정보는 로그에 마스킹
```java
// ❌ 나쁜 예
log.info("User login: {}", email);

// ✅ 좋은 예
log.info("User login: {}", MaskingUtils.maskEmail(email));
```

### 3. 개인정보는 암호화 저장
```java
// ❌ 나쁜 예
user.setResidentNumber(rrn);

// ✅ 좋은 예
String encrypted = EncryptionUtils.encryptAES256(rrn, SECRET_KEY);
user.setResidentNumber(encrypted);
```

### 4. 입력값은 서버에서도 검증
```java
// ❌ 나쁜 예 (프론트엔드 검증만 믿음)
emailService.send(email, message);

// ✅ 좋은 예
ValidationUtils.requireValidEmail(email, "이메일");
emailService.send(email, message);
```

---

## 📚 추가 리소스

- Apache Commons Lang3: 기본적인 문자열/날짜 처리
- JSoup Documentation: HTML 필터링 고급 기능
- Spring Security Documentation: 인증/인가

---

**Happy Coding! 🚀**

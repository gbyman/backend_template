# Validation Annotations 가이드

Spring Boot Bean Validation을 확장한 커스텀 검증 어노테이션 가이드입니다.

## 📋 목차

- [파일 검증](#파일-검증)
  - [@ValidFileExtension](#validfileextension)
  - [@ValidImageFile](#validimagefile)
  - [@ValidMimeType](#validmimetype)
  - [@NoMaliciousFile](#nomaliciousfile)
- [보안 검증](#보안-검증)
  - [@NoXSS](#noxss)
  - [@NoSQLInjection](#nosqlinjection)
  - [@SafePath](#safepath)
  - [@ValidPassword](#validpassword)

---

## 파일 검증

### @ValidFileExtension

파일 확장자와 크기를 검증합니다. `application.yml`에 설정된 기본값을 사용하거나, 어노테이션에서 개별 지정 가능합니다.

**설정 우선순위**: 어노테이션 값 > application.yml 기본값

#### application.yml 설정

```yaml
file-upload:
  allowed-extensions:
    - jpg
    - jpeg
    - png
    - gif
    - pdf
    - doc
    - docx
    - xls
    - xlsx
    - ppt
    - pptx
    - txt
    - zip
  max-size-in-bytes: 10485760  # 10MB
```

#### 사용 예시

```java
// 1. yml 기본값 사용
public class DocumentDto {
    @ValidFileExtension
    private List<MultipartFile> documents;
}

// 2. 특정 확장자만 허용 (yml 오버라이드)
public class ProfileImageDto {
    @ValidFileExtension(allowed = {"jpg", "png", "gif"})
    private List<MultipartFile> profileImage;
}

// 3. 크기만 변경 (확장자는 yml 사용)
public class AttachmentDto {
    @ValidFileExtension(maxSizeInBytes = 20971520)  // 20MB
    private List<MultipartFile> attachments;
}

// 4. 완전 커스텀
public class VideoDto {
    @ValidFileExtension(
        allowed = {"mp4", "avi", "mov"},
        maxSizeInBytes = 104857600  // 100MB
    )
    private List<MultipartFile> videos;
}
```

#### 검증 항목
- ✅ 허용된 확장자인지 확인
- ✅ 파일 크기 제한 확인
- ✅ 빈 파일 건너뛰기

---

### @ValidImageFile

실제 이미지 파일인지 검증합니다. 파일 확장자가 아닌 **magic number**(파일 헤더)를 검사하여 확장자 위조 공격을 방지합니다.

#### 사용 예시

```java
public class ImageUploadDto {
    // 기본 설정 (jpg, jpeg, png, gif, webp)
    @ValidImageFile
    private MultipartFile image;

    // 특정 포맷만 허용
    @ValidImageFile(allowedFormats = {"jpg", "png"})
    private MultipartFile thumbnail;

    // 크기 제한 포함
    @ValidImageFile(
        allowedFormats = {"jpg", "png"},
        maxSizeInBytes = 5242880  // 5MB
    )
    private MultipartFile avatar;
}
```

#### 검증 항목
- ✅ 실제 이미지 파일인지 magic number로 검증
- ✅ JPG, PNG, GIF, WebP 포맷 지원
- ✅ 파일 크기 제한 (선택)
- ⚠️ 확장자 위조 공격 방지

#### Magic Numbers

| 포맷 | Magic Number |
|------|-------------|
| JPG  | FF D8 FF |
| PNG  | 89 50 4E 47 |
| GIF  | 47 49 46 38 |
| WebP | 52 49 46 46 (RIFF) + WEBP |

---

### @ValidMimeType

파일의 MIME 타입을 검증합니다.

#### 사용 예시

```java
public class FileUploadDto {
    @ValidMimeType(allowed = {"image/jpeg", "image/png"})
    private MultipartFile image;

    @ValidMimeType(allowed = {"application/pdf"})
    private MultipartFile document;

    @ValidMimeType(
        allowed = {"video/mp4", "video/quicktime"},
        maxSizeInBytes = 52428800  // 50MB
    )
    private MultipartFile video;
}
```

#### 검증 항목
- ✅ Content-Type 검증
- ✅ 허용된 MIME 타입 확인
- ✅ 파일 크기 제한 (선택)

---

### @NoMaliciousFile

위험한 파일 확장자를 차단합니다.

#### 사용 예시

```java
public class SafeFileUploadDto {
    @NoMaliciousFile
    private MultipartFile file;

    @NoMaliciousFile(maxSizeInBytes = 10485760)  // 10MB
    private List<MultipartFile> attachments;
}
```

#### 차단되는 확장자

```java
exe, bat, cmd, com, scr, pif, vbs, js, jar, msi,
dll, sh, bash, ps1, app, deb, rpm, run, bin,
php, jsp, asp, aspx, py, rb, pl, cgi, sql
```

#### 검증 항목
- ✅ 위험한 실행 파일 확장자 차단
- ✅ 이중 확장자 검사 (예: `file.pdf.exe`)
- ✅ 경로 탐색 패턴 차단
- ✅ 숨김 파일 차단 (`.` 시작)

---

## 보안 검증

### @NoXSS

XSS(Cross-Site Scripting) 공격 패턴을 차단합니다.

#### 사용 예시

```java
public class CommentDto {
    @NoXSS
    private String content;

    @NoXSS
    private String title;
}

public class SearchDto {
    @NoXSS
    private String keyword;
}
```

#### 차단되는 패턴

```javascript
// 스크립트 태그
<script>, </script>, <script>alert('XSS')</script>

// 이벤트 핸들러
onerror=, onclick=, onload=, onmouseover=

// JavaScript 프로토콜
javascript:, data:text/html

// 위험한 태그
<iframe>, <object>, <embed>, <applet>

// JavaScript 함수
eval(), setTimeout(), setInterval()

// 인코딩된 패턴
&#, &#x, \u, %3C (URL 인코딩)
```

#### 검증 항목
- ✅ 20+ XSS 패턴 차단
- ✅ 대소문자 구분 없이 검사
- ✅ URL 인코딩 패턴 감지

---

### @NoSQLInjection

SQL Injection 공격 패턴을 차단합니다.

#### 사용 예시

```java
public class SearchDto {
    @NoSQLInjection
    private String keyword;
}

public class LoginDto {
    @NoSQLInjection
    private String username;
}
```

#### 차단되는 패턴

```sql
-- SQL 주석
--, /*, */

-- SQL 키워드
SELECT, INSERT, UPDATE, DELETE, DROP, UNION,
ALTER, CREATE, TRUNCATE, GRANT, REVOKE

-- 특수 문자 조합
'; --, ' OR '1'='1, ' OR 1=1--

-- 함수 호출
EXEC, EXECUTE, sp_executesql

-- 시간 기반 공격
SLEEP, WAITFOR DELAY, BENCHMARK
```

#### 검증 항목
- ✅ 20+ SQL Injection 패턴 차단
- ✅ 대소문자 구분 없이 검사
- ✅ 주석 패턴 감지
- ✅ UNION 공격 차단

---

### @SafePath

경로 탐색(Path Traversal) 공격을 방지합니다.

#### 사용 예시

```java
public class FileAccessDto {
    // 상대 경로만 허용
    @SafePath
    private String filePath;

    // 절대 경로 허용
    @SafePath(allowAbsolute = true)
    private String absolutePath;
}
```

#### 차단되는 패턴

```bash
# 경로 탐색
../, ..\, /../, \..\

# Null byte injection
\0, %00

# 특수 문자
<, >, :, ", |, ?, *

# Unix 특수 경로
/dev/, /proc/, /sys/

# URL 인코딩 시도
%2e%2e/, %2f

# 이중 슬래시
/\, \/
```

#### 검증 항목
- ✅ 디렉토리 탐색 패턴 차단
- ✅ Null byte injection 방지
- ✅ 절대 경로 제어 (옵션)
- ✅ URL 인코딩 탐색 감지
- ✅ 유효한 경로 문법 검사

---

### @ValidPassword

비밀번호 강도를 검증합니다.

#### 사용 예시

```java
// 1. 기본 설정 (8~16자, 영문+숫자+특수문자)
public class SignUpDto {
    @ValidPassword
    private String password;
}

// 2. 커스텀 길이
public class CustomPasswordDto {
    @ValidPassword(minLength = 10, maxLength = 20)
    private String password;
}

// 3. 특수문자 변경
public class PasswordDto {
    @ValidPassword(specialChars = "@$!%*?&#")
    private String password;
}

// 4. 숫자만 필수 (PIN 코드)
public class PinDto {
    @ValidPassword(
        requireLetters = false,
        requireSpecialChars = false,
        minLength = 4,
        maxLength = 6
    )
    private String pin;
}

// 5. 완전 커스텀
public class StrongPasswordDto {
    @ValidPassword(
        minLength = 12,
        maxLength = 32,
        requireLetters = true,
        requireNumbers = true,
        requireSpecialChars = true,
        specialChars = "@$!%*?&"
    )
    private String password;
}
```

#### 검증 항목
- ✅ 길이 제한 (기본: 8~16자)
- ✅ 영문자 포함 여부 (선택)
- ✅ 숫자 포함 여부 (선택)
- ✅ 특수문자 포함 여부 (선택)
- ✅ 허용된 문자만 사용했는지 검사

#### 기본 설정

| 속성 | 기본값 | 설명 |
|------|--------|------|
| minLength | 8 | 최소 길이 |
| maxLength | 16 | 최대 길이 |
| requireLetters | true | 영문자 필수 |
| requireNumbers | true | 숫자 필수 |
| requireSpecialChars | true | 특수문자 필수 |
| specialChars | `@$!%*?&` | 허용 특수문자 |

---

## 💡 사용 팁

### 1. 여러 검증 조합

```java
public class SecureFileUploadDto {
    @NotNull
    @ValidFileExtension(allowed = {"jpg", "png"})
    @ValidImageFile
    @NoMaliciousFile
    private MultipartFile profileImage;
}
```

### 2. Controller에서 검증

```java
@RestController
@RequestMapping("/api/v1/files")
public class FileController extends AbstractController {

    @PostMapping("/upload")
    public BizRespVo<String> upload(@Valid @RequestBody FileUploadDto dto) {
        // @Valid가 자동으로 모든 검증 수행
        return super.makeResponse("업로드 성공");
    }
}
```

### 3. 검증 실패 시 응답

검증 실패 시 `BizException`이 발생하며, GlobalExceptionHandler가 처리합니다:

```json
{
  "status": 400,
  "code": "ERR0001",
  "message": "파일 'malware.exe': 위험한 파일 확장자입니다.",
  "data": null,
  "timestamp": "2024-02-14T12:00:00"
}
```

### 4. 검증 그룹 사용

```java
public interface CreateGroup {}
public interface UpdateGroup {}

public class UserDto {
    @ValidPassword(groups = CreateGroup.class)
    private String password;

    @ValidPassword(groups = UpdateGroup.class)
    private String newPassword;
}

// Controller
@PostMapping
public BizRespVo<UserDto> create(@Validated(CreateGroup.class) @RequestBody UserDto dto) {
    // ...
}
```

---

## 🔧 확장 방법

새로운 검증 어노테이션을 추가하려면:

### 1. 어노테이션 생성

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MyCustomValidator.class)
public @interface MyCustomValidation {
    String message() default "커스텀 검증 실패";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 2. Validator 생성

```java
public class MyCustomValidator implements ConstraintValidator<MyCustomValidation, String> {

    @Override
    public void initialize(MyCustomValidation annotation) {
        // 초기화
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;  // null 체크는 @NotNull로
        }

        // 검증 로직
        if (!isValidValue(value)) {
            throw new BizException(
                HttpStatus.BAD_REQUEST,
                MessageConstants.BAD_REQUEST,
                "검증 실패 메시지"
            );
        }

        return true;
    }
}
```

---

## 📚 참고

- 모든 validator는 `null` 또는 빈 값을 허용합니다 (required 체크는 `@NotNull`, `@NotBlank` 사용)
- 검증 실패 시 `BizException`을 발생시켜 일관된 에러 응답 제공
- `GlobalExceptionHandler`가 자동으로 예외를 처리하여 클라이언트에 응답

---

**Happy Coding! 🚀**

# WebClient HTTP 클라이언트

Spring WebFlux 기반 HTTP 클라이언트 헬퍼

---

## 🎯 주요 기능

- **Reactive HTTP 통신** - WebClient 기반 비동기 처리
- **Mono/Flux 지원** - 단일/다중 응답 처리
- **자동 Multipart 감지** - 파일 업로드 자동 처리
- **타임아웃 설정** - 연결/읽기/쓰기 타임아웃 (기본 120초)
- **요청/응답 로깅** - 자동 로그 기록
- **에러 처리** - 4xx/5xx 에러 로깅

---

## 🚀 빠른 시작

### 1. 설정 (application.yml)

```yaml
webclient:
  timeout:
    seconds: 120  # 기본값: 120초
```

### 2. 기본 사용법

#### GET 요청 (Mono)
```java
@Service
@RequiredArgsConstructor
public class ApiService {

    private final WebClientHelper webClientHelper;

    public UserDto getUser(Long userId) {
        return webClientHelper
            .getMono(
                "https://api.example.com/users/" + userId,
                null,  // headers
                null,  // queryParams
                UserDto.class
            )
            .block();  // 동기 변환 (비동기로 사용하려면 block() 제거)
    }
}
```

#### POST 요청 (Mono)
```java
public UserDto createUser(CreateUserRequest request) {
    return webClientHelper
        .postMono(
            "https://api.example.com/users",
            request,  // body
            null,     // headers
            null,     // queryParams
            UserDto.class
        )
        .block();
}
```

#### GET 요청 (Flux - 리스트)
```java
public List<UserDto> getUsers() {
    return webClientHelper
        .getFlux(
            "https://api.example.com/users",
            null,
            null,
            UserDto.class
        )
        .collectList()
        .block();
}
```

---

## 📋 메서드 목록

### Mono (단일 응답)

| 메서드 | 설명 |
|--------|------|
| `getMono()` | GET 요청, body 반환 |
| `getEntity()` | GET 요청, ResponseEntity 반환 |
| `postMono()` | POST 요청 |
| `postEntity()` | POST 요청, ResponseEntity 반환 |
| `putMono()` | PUT 요청 |
| `deleteMono()` | DELETE 요청 |

### Flux (다중 응답)

| 메서드 | 설명 |
|--------|------|
| `getFlux()` | GET 요청 (스트림) |
| `postFlux()` | POST 요청 (스트림) |
| `putFlux()` | PUT 요청 (스트림) |
| `deleteFlux()` | DELETE 요청 (스트림) |

---

## 🔧 고급 사용법

### 1. 헤더 추가
```java
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth("your-token");
headers.set("Custom-Header", "value");

UserDto user = webClientHelper
    .getMono("https://api.example.com/users/1", headers, null, UserDto.class)
    .block();
```

### 2. 쿼리 파라미터
```java
Map<String, Object> queryParams = Map.of(
    "page", 1,
    "size", 10,
    "sort", "name"
);

List<UserDto> users = webClientHelper
    .getFlux("https://api.example.com/users", null, queryParams, UserDto.class)
    .collectList()
    .block();
```

### 3. 파일 업로드 (Multipart)

**DTO에 MultipartFile 포함 시 자동 감지:**

```java
@Data
public class UploadRequest {
    private String title;
    private MultipartFile file;
}

// 사용
UploadRequest request = new UploadRequest();
request.setTitle("문서");
request.setFile(multipartFile);

ResponseDto response = webClientHelper
    .postMono("https://api.example.com/upload", request, null, null, ResponseDto.class)
    .block();
```

### 4. 제네릭 타입 응답
```java
// List<UserDto> 같은 제네릭 타입 처리
ParameterizedTypeReference<List<UserDto>> typeRef = new ParameterizedTypeReference<>() {};

List<UserDto> users = webClientHelper
    .getMono("https://api.example.com/users", null, null, typeRef)
    .block();
```

### 5. 비동기 처리
```java
// block() 없이 Mono 반환
public Mono<UserDto> getUserAsync(Long userId) {
    return webClientHelper.getMono(
        "https://api.example.com/users/" + userId,
        null,
        null,
        UserDto.class
    );
}

// 사용
getUserAsync(1L)
    .subscribe(user -> System.out.println(user.getName()));
```

### 6. ResponseEntity로 상태 코드 확인
```java
ResponseEntity<UserDto> response = webClientHelper
    .getEntity("https://api.example.com/users/1", null, null, UserDto.class)
    .block();

if (response.getStatusCode().is2xxSuccessful()) {
    UserDto user = response.getBody();
    HttpHeaders headers = response.getHeaders();
}
```

---

## ⚙️ 설정

### 타임아웃 변경
```yaml
webclient:
  timeout:
    seconds: 60  # 60초로 변경
```

### 메모리 버퍼 크기
기본값: 50MB (WebClientConfig 수정 필요)

```java
.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
```

---

## 📊 로깅

### 요청 로그
```
INFO : Request:GET https://api.example.com/users/1
INFO : Authorization=Bearer xxx
INFO : Content-Type=application/json
```

### 에러 로그
```
ERROR: Client Error: 404 NOT_FOUND
ERROR: Server Error: 500 INTERNAL_SERVER_ERROR
ERROR: Error during request: Connection timeout
```

---

## 🛠️ 트러블슈팅

### Q1. Timeout 발생
```yaml
# 타임아웃 늘리기
webclient:
  timeout:
    seconds: 300  # 5분
```

### Q2. 파일 업로드 실패
- DTO에 `MultipartFile` 필드가 있는지 확인
- `Content-Type: multipart/form-data`로 자동 설정됨

### Q3. SSL 인증서 오류
WebClientConfig에서 SSL 무시 설정 추가 (개발 환경만!)

---

## 📚 참고

- [Spring WebFlux 공식 문서](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Reactor 공식 문서](https://projectreactor.io/)
- [Project Reactor - Mono/Flux](https://projectreactor.io/docs/core/release/reference/)

---

**Happy Coding! 🚀**

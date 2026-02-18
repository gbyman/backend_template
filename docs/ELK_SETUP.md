# ELK Stack (Elasticsearch + Logstash + Kibana) 설정 가이드

## 개요

ELK Stack을 사용하여 애플리케이션 로그를 중앙 집중화하고, 실시간으로 검색/분석/시각화할 수 있습니다.

---

## ELK Stack 구성 요소

### 📊 **Elasticsearch**
- **역할**: 로그 데이터 저장 및 검색 엔진
- **포트**: 9200 (HTTP), 9300 (TCP)
- **기능**:
  - 빠른 전문 검색
  - 대용량 로그 저장
  - 집계 및 분석

### 🔄 **Logstash**
- **역할**: 로그 수집, 변환, 전송 파이프라인
- **포트**: 5000 (TCP), 5044 (Beats)
- **기능**:
  - 로그 파싱 및 필터링
  - 다양한 입력/출력 지원
  - 데이터 변환 및 정규화

### 📈 **Kibana**
- **역할**: 데이터 시각화 및 대시보드
- **포트**: 5601 (HTTP)
- **기능**:
  - 로그 검색 인터페이스
  - 실시간 대시보드
  - 알림 및 모니터링

### 🚀 **Filebeat** (선택)
- **역할**: 경량 로그 수집기
- **기능**:
  - 로그 파일 실시간 수집
  - 낮은 리소스 사용
  - 안정적인 데이터 전송

---

## 아키텍처

### 방식 1: Logback → JSON 파일 → Filebeat → Logstash → Elasticsearch
```
Application (Logback)
    ↓ (JSON 파일 출력)
logs/application-json.log
    ↓ (파일 읽기)
Filebeat
    ↓ (전송)
Logstash
    ↓ (필터링/변환)
Elasticsearch
    ↓ (시각화)
Kibana
```

### 방식 2: Logback → TCP → Logstash → Elasticsearch (직접 전송)
```
Application (Logback)
    ↓ (TCP 5000)
Logstash
    ↓ (필터링/변환)
Elasticsearch
    ↓ (시각화)
Kibana
```

### 방식 3: Logback → Elasticsearch (Logstash 없이 직접 전송)
```
Application (Logback)
    ↓ (HTTP Bulk API)
Elasticsearch
    ↓ (시각화)
Kibana
```

**권장**:
- **방식 1**: 안정성이 높고, 애플리케이션에 부하 적음 (프로덕션 환경)
- **방식 2**: 실시간 로깅, Logstash 필터링 활용 시
- **방식 3**: 단순한 구성, Logstash 인프라 불필요 (소규모 환경)

---

## 1. 라이브러리 추가

이미 `module-common/build.gradle`에 추가되어 있습니다:

```gradle
// 방식 1, 2: JSON 로그 포맷 + Logstash 전송
api 'net.logstash.logback:logstash-logback-encoder:8.0'

// 방식 3: Elasticsearch 직접 전송 (Logstash 없이)
api 'com.internetitem:logback-elasticsearch-appender:1.6'
```

---

## 2. Logback 설정

### ELK 기능 활성화

ELK 기능은 `logback-spring.xml`에 통합되어 있으며, **프로필로 선택 활성화**됩니다.

#### 방식 1, 2: Logstash 사용 (elk 프로필)

**환경변수로 프로필 설정:**
```bash
# ELK 기능 활성화 (dev + elk)
export SPRING_PROFILES_ACTIVE=dev,elk

# ELK 기능 활성화 (prod + elk)
export SPRING_PROFILES_ACTIVE=prod,elk
```

**실행 시 지정:**
```bash
./gradlew :module-api:bootRun --args='--spring.profiles.active=dev,elk'
```

**환경변수 설정:**
```bash
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200
export LOGSTASH_HOST=localhost
export LOGSTASH_PORT=5000
```

#### 방식 3: Elasticsearch 직접 전송 (elasticsearch 프로필)

**Logstash 없이 Elasticsearch로 직접 전송:**
```bash
# Elasticsearch 직접 전송 (dev + elasticsearch)
export SPRING_PROFILES_ACTIVE=dev,elasticsearch

# Elasticsearch 직접 전송 (prod + elasticsearch)
export SPRING_PROFILES_ACTIVE=prod,elasticsearch
```

**실행 시 지정:**
```bash
./gradlew :module-api:bootRun --args='--spring.profiles.active=dev,elasticsearch'
```

**환경변수 설정 (Elasticsearch만 필요):**
```bash
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200
# Logstash 불필요
```

**특징:**
- Logstash, Filebeat 없이 Elasticsearch만 실행
- 애플리케이션에서 HTTP Bulk API로 직접 전송
- MDC 데이터 자동 포함
- 비동기 전송으로 성능 최적화

---

## 3. Docker Compose로 ELK Stack 실행

### 방식 1, 2: Full ELK Stack (Elasticsearch + Logstash + Kibana + Filebeat)

#### Step 1: 디렉토리 구조 확인
```
backend_template/
├── docker-compose.elk.yml
├── elk/
│   ├── logstash/
│   │   ├── config/logstash.yml
│   │   └── pipeline/logstash.conf
│   └── filebeat/
│       └── filebeat.yml
└── logs/
```

#### Step 2: 디렉토리 생성
```bash
mkdir -p elk/logstash/config elk/logstash/pipeline elk/filebeat logs
```

#### Step 3: ELK Stack 시작
```bash
docker-compose -f docker-compose.elk.yml up -d
```

### 방식 3: Elasticsearch + Kibana만 실행 (간단한 구성)

**Logstash, Filebeat 없이 Elasticsearch와 Kibana만 실행:**

```bash
# Elasticsearch + Kibana만 시작
docker-compose -f docker-compose.elasticsearch.yml up -d
```

**docker-compose.elasticsearch.yml 파일 생성:**
```yaml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - elk

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - elk

networks:
  elk:
    driver: bridge

volumes:
  elasticsearch-data:
```

### Step 4: 상태 확인
```bash
# 컨테이너 확인
docker-compose -f docker-compose.elk.yml ps

# Elasticsearch 확인
curl http://localhost:9200

# Kibana 확인 (브라우저)
http://localhost:5601
```

### Step 5: 애플리케이션 시작

#### Full ELK Stack 사용 시 (방식 1, 2)
```bash
# ELK 프로필 활성화
export SPRING_PROFILES_ACTIVE=dev,elk
export LOGSTASH_HOST=localhost
export LOGSTASH_PORT=5000
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200

# 애플리케이션 실행
./gradlew :module-api:bootRun
```

#### Elasticsearch 직접 전송 사용 시 (방식 3)
```bash
# Elasticsearch 프로필 활성화
export SPRING_PROFILES_ACTIVE=dev,elasticsearch
export ELASTICSEARCH_HOST=localhost
export ELASTICSEARCH_PORT=9200

# 애플리케이션 실행
./gradlew :module-api:bootRun
```

**로그 확인:**
```bash
# 애플리케이션 로그 발생
curl http://localhost:8080/api/v1/samples/hello

# Elasticsearch에서 로그 확인
curl http://localhost:9200/backend-logs-*/_search?pretty
```

---

## 4. Kibana 설정

### Step 1: Kibana 접속
```
http://localhost:5601
```

### Step 2: Index Pattern 생성
1. **Menu** > **Stack Management** > **Index Patterns**
2. **Create index pattern**
3. **Index pattern name**: `backend-logs-*`
4. **Timestamp field**: `@timestamp`
5. **Create index pattern**

### Step 3: Discover에서 로그 확인
1. **Menu** > **Discover**
2. Index pattern 선택: `backend-logs-*`
3. 로그 검색 및 필터링

### Step 4: 대시보드 생성
1. **Menu** > **Dashboard** > **Create dashboard**
2. **Add panel**
3. 시각화 추가:
   - **로그 레벨별 분포** (Pie chart)
   - **시간별 로그 발생 추이** (Line chart)
   - **에러 로그 TOP 10** (Data table)

---

## 5. 로그 검색 쿼리 예시

### Kibana Query (KQL)

**에러 로그만 검색:**
```
level: ERROR
```

**특정 HTTP 상태 코드:**
```
requestStatus: 500
```

**느린 응답 검색 (1초 이상):**
```
responseTimeMs >= 1000
```

**특정 사용자:**
```
userId: "admin"
```

**특정 API 엔드포인트:**
```
requestUri: "/api/v1/users/*"
```

**에러 타입별 검색:**
```
errorClass: "BizException"
```

**비즈니스 에러 코드 검색:**
```
bizErrorCode: "USER_NOT_FOUND"
```

**복합 조건 (에러 + 특정 사용자 + 특정 API):**
```
level: ERROR AND userId: "admin" AND requestUri: "/api/v1/users/*"
```

**응답 시간별 성능 분석:**
```
requestMethod: "GET" AND responseTimeMs > 500
```

**4xx 에러 (클라이언트 에러) 검색:**
```
requestStatus >= 400 AND requestStatus < 500
```

**5xx 에러 (서버 에러) 검색:**
```
requestStatus >= 500
```

**특정 요청 추적 (Trace ID로):**
```
traceId: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**특정 IP에서 발생한 에러:**
```
level: ERROR AND clientIp: "192.168.1.100"
```

**특정 IP 대역 검색:**
```
clientIp: 192.168.1.*
```

**IP별 요청 패턴 분석:**
```
clientIp: "10.0.0.50" AND requestUri: "/api/v1/users/*"
```

---

## 6. MDC (Mapped Diagnostic Context) 사용

### 자동 MDC 설정 (RequestLoggingFilter)

프로젝트에는 **RequestLoggingFilter**가 이미 구현되어 있어, 요청마다 자동으로 MDC를 설정합니다.

**자동 설정되는 MDC 필드:**
- `requestId`: 요청마다 생성되는 고유 ID (UUID)
- `traceId`: 분산 추적 ID (헤더의 X-Trace-Id 또는 requestId)
- `userId`: 인증된 사용자 ID (SecurityContext에서 자동 추출)
- `requestUri`: 요청 URI
- `requestMethod`: HTTP 메서드 (GET, POST 등)
- `requestStatus`: 응답 상태 코드 (200, 404, 500 등)
- `responseTimeMs`: 응답 시간 (밀리초)
- `clientIp`: 클라이언트 IP 주소 (X-Forwarded-For, X-Real-IP 등 프록시 헤더 지원)
- `errorMessage`: 에러 메시지 (에러 발생 시)
- `errorClass`: 에러 클래스명 (에러 발생 시)
- `errorStatus`: 에러 상태 코드 (에러 발생 시)
- `errorCode`: 에러 코드 (에러 발생 시)
- `bizErrorCode`: 비즈니스 에러 코드 (BizException 발생 시)

### RequestLoggingFilter 동작 방식

```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 요청 시작 시 MDC 설정
        setupMDC(request); // requestId, traceId, userId 등 설정

        try {
            // 2. 요청 처리
            filterChain.doFilter(request, response);

            // 3. 응답 성공 시 MDC에 상태 정보 추가
            MDC.put("requestStatus", String.valueOf(response.getStatus()));
            MDC.put("responseTimeMs", String.valueOf(responseTime));

        } catch (Exception e) {
            // 4. 에러 발생 시 MDC에 에러 정보 추가
            MDC.put("errorMessage", e.getMessage());
            MDC.put("errorClass", e.getClass().getName());
        } finally {
            // 5. MDC 정리
            MDC.clear();
        }
    }
}
```

### 커스텀 MDC 필드 추가하기

비즈니스 로직에서 추가 정보를 MDC에 설정할 수 있습니다:

```java
import org.slf4j.MDC;

@Service
public class OrderService {
    public void createOrder(String orderId, String customerId) {
        // MDC에 비즈니스 정보 추가
        MDC.put("orderId", orderId);
        MDC.put("customerId", customerId);

        try {
            log.info("Creating order");
            // 주문 처리 로직
        } finally {
            // 추가한 MDC 정리 (필수)
            MDC.remove("orderId");
            MDC.remove("customerId");
        }
    }
}
```

### logback-spring.xml에 MDC 필드 포함 (이미 설정됨)

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"app":"${APP_NAME}","env":"${SPRING_PROFILES_ACTIVE:-local}"}</customFields>
    <!-- MDC 필드 포함 -->
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>spanId</includeMdcKeyName>
    <includeMdcKeyName>userId</includeMdcKeyName>
    <includeMdcKeyName>requestId</includeMdcKeyName>
    <includeMdcKeyName>requestUri</includeMdcKeyName>
    <includeMdcKeyName>requestMethod</includeMdcKeyName>
    <includeMdcKeyName>requestStatus</includeMdcKeyName>
    <includeMdcKeyName>responseTimeMs</includeMdcKeyName>
    <includeMdcKeyName>clientIp</includeMdcKeyName>
    <includeMdcKeyName>errorMessage</includeMdcKeyName>
    <includeMdcKeyName>errorClass</includeMdcKeyName>
    <includeMdcKeyName>errorStatus</includeMdcKeyName>
    <includeMdcKeyName>errorCode</includeMdcKeyName>
    <includeMdcKeyName>bizErrorCode</includeMdcKeyName>
</encoder>
```

---

## 7. JSON 로그 포맷 예시

### 정상 요청 로그 (application-json.log)
```json
{
  "@timestamp": "2026-02-14T10:30:45.123Z",
  "@version": "1",
  "message": "<<< Response: GET /api/v1/users/123 | Status: 200 | Time: 45ms",
  "logger_name": "app.backend.api.filter.RequestLoggingFilter",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "app": "backend-template",
  "env": "prod",
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": "admin",
  "requestUri": "/api/v1/users/123",
  "requestMethod": "GET",
  "requestStatus": "200",
  "responseTimeMs": "45",
  "clientIp": "192.168.1.100"
}
```

### 에러 로그 예시 (error-json.log)
```json
{
  "@timestamp": "2026-02-14T10:35:12.456Z",
  "@version": "1",
  "message": ">>> error: User not found",
  "logger_name": "app.backend.core.exception.GlobalExceptionHandler",
  "thread_name": "http-nio-8080-exec-3",
  "level": "ERROR",
  "level_value": 40000,
  "stack_trace": "app.backend.core.base.exception.BizException: User not found\n\tat app.backend.app.user.service.UserService.getUser(UserService.java:42)\n\tat ...",
  "app": "backend-template",
  "env": "prod",
  "requestId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "traceId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "userId": "admin",
  "requestUri": "/api/v1/users/999",
  "requestMethod": "GET",
  "requestStatus": "404",
  "responseTimeMs": "12",
  "clientIp": "192.168.1.100",
  "errorMessage": "User not found",
  "errorClass": "BizException",
  "errorStatus": "404",
  "errorCode": "NOT_FOUND",
  "bizErrorCode": "USER_NOT_FOUND"
}
```

### Validation 에러 로그 예시
```json
{
  "@timestamp": "2026-02-14T10:36:00.789Z",
  "level": "ERROR",
  "message": ">>> validation error: email: must be a well-formed email address",
  "logger_name": "app.backend.core.exception.GlobalExceptionHandler",
  "thread_name": "http-nio-8080-exec-5",
  "app": "backend-template",
  "env": "prod",
  "requestId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "traceId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "userId": "user123",
  "requestUri": "/api/v1/users",
  "requestMethod": "POST",
  "requestStatus": "400",
  "responseTimeMs": "8",
  "clientIp": "10.0.0.50",
  "errorMessage": "email: must be a well-formed email address",
  "errorClass": "MethodArgumentNotValidException",
  "errorStatus": "400",
  "errorCode": "BAD_REQUEST"
}
```

---

## 8. 성능 최적화

### 비동기 Appender 사용
```xml
<appender name="ASYNC_LOGSTASH" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="LOGSTASH_TCP"/>
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <neverBlock>false</neverBlock>
</appender>
```

### Logstash 큐 크기 조정
```conf
# logstash.conf
input {
  tcp {
    port => 5000
    codec => json_lines
  }
}

# logstash.yml
queue.type: persisted
queue.max_bytes: 1gb
```

### Elasticsearch 인덱스 최적화
```bash
# Index Lifecycle Management (ILM) 설정
PUT _ilm/policy/backend-logs-policy
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "7d"
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

---

## 9. Kubernetes 환경

### ConfigMap (Filebeat)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: log
      paths:
        - /var/log/backend/*.log
      json.keys_under_root: true

    output.elasticsearch:
      hosts: ["elasticsearch-service:9200"]
      index: "backend-logs-%{+yyyy.MM.dd}"
```

### DaemonSet (Filebeat)
```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: filebeat
spec:
  template:
    spec:
      containers:
      - name: filebeat
        image: docker.elastic.co/beats/filebeat:8.11.0
        volumeMounts:
        - name: config
          mountPath: /usr/share/filebeat/filebeat.yml
          subPath: filebeat.yml
        - name: logs
          mountPath: /var/log/backend
      volumes:
      - name: config
        configMap:
          name: filebeat-config
      - name: logs
        hostPath:
          path: /var/log/containers
```

---

## 10. 트러블슈팅

### Logstash 연결 실패
```
ERROR: Connection refused (Connection refused)
```

**해결방법:**
- Logstash 컨테이너 실행 확인: `docker ps | grep logstash`
- 포트 확인: `netstat -an | grep 5000`
- 방화벽 확인

### Elasticsearch 디스크 부족
```
cluster_block_exception: blocked by: [FORBIDDEN/12/index read-only]
```

**해결방법:**
```bash
# Read-only 모드 해제
curl -X PUT "localhost:9200/_cluster/settings" \
  -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.disk.threshold_enabled": false
  }
}'

# 오래된 인덱스 삭제
curl -X DELETE "localhost:9200/backend-logs-2024.01.*"
```

### Kibana 인덱스 패턴 없음
**해결방법:**
1. 로그가 실제로 Elasticsearch에 저장되었는지 확인:
   ```bash
   curl http://localhost:9200/_cat/indices?v
   ```
2. 인덱스가 없으면 애플리케이션에서 로그 발생시키기
3. Kibana에서 인덱스 패턴 재생성

---

## 11. 모니터링 및 알림

### Elastic Watcher로 알림 설정

**에러 발생 시 Slack 알림:**
```json
PUT _watcher/watch/error-alert
{
  "trigger": {
    "schedule": {
      "interval": "1m"
    }
  },
  "input": {
    "search": {
      "request": {
        "indices": ["backend-logs-*"],
        "body": {
          "query": {
            "bool": {
              "must": [
                { "match": { "level": "ERROR" }},
                { "range": { "@timestamp": { "gte": "now-1m" }}}
              ]
            }
          }
        }
      }
    }
  },
  "condition": {
    "compare": {
      "ctx.payload.hits.total": {
        "gte": 10
      }
    }
  },
  "actions": {
    "send_slack": {
      "webhook": {
        "url": "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK",
        "body": "Error count: {{ctx.payload.hits.total}}"
      }
    }
  }
}
```

---

## 참고 문서

- [Elastic Stack Documentation](https://www.elastic.co/guide/index.html)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [Filebeat Reference](https://www.elastic.co/guide/en/beats/filebeat/current/index.html)
- [Kibana Guide](https://www.elastic.co/guide/en/kibana/current/index.html)

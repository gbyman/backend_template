package app.backend.core.entity;

import java.time.LocalDateTime;

import app.backend.core.base.entity.BaseEntity;
import app.backend.core.enums.LogCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시스템 접근 로그 엔티티
 *
 * <p>모든 API 요청과 페이지 접근 이력을 저장합니다.
 *
 * <p><strong>주요 기능:</strong>
 *
 * <ul>
 *   <li>API 접근 이력 추적 (요청 ID, URI, 메서드)
 *   <li>로그 카테고리 자동 분류 (PAGE_VIEW, API_CALL, ERROR 등)
 *   <li>사용자 및 클라이언트 IP 기록
 *   <li>요청/응답 시간 및 실행 시간 측정
 *   <li>요청 파라미터 JSON 저장
 *   <li>에러 메시지 기록
 * </ul>
 *
 * <p><strong>테이블 구조:</strong>
 *
 * <ul>
 *   <li>테이블명: tb_sys_log
 *   <li>파티셔닝: 월별 파티션 (REQUEST_TIME 기준)
 *   <li>인덱스: PK만 (LOG_ID, REQUEST_TIME) - INSERT 성능 우선
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * SysLog log = SysLog.builder()
 *     .requestId("req-123")
 *     .requestUri("/api/users")
 *     .httpMethod("GET")
 *     .logCategory(LogCategory.API_CALL)
 *     .clientIp("127.0.0.1")
 *     .userId("user123")
 *     .requestParams("{\"page\":1,\"size\":10}")
 *     .requestTime(LocalDateTime.now())
 *     .responseTime(LocalDateTime.now())
 *     .executionTimeMs(150)
 *     .statusCode(200)
 *     .build();
 * </pre>
 *
 * @see app.backend.core.enums.LogCategory
 * @see app.backend.core.aspect.DatabaseLoggingAspect
 */
@Entity
@Table(name = "tb_sys_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SysLog extends BaseEntity {

    /** 로그 ID (자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    /** 요청 ID (UUID) - 요청 추적용 */
    @Column(name = "REQUEST_ID", nullable = false, length = 36)
    private String requestId;

    /** 요청 URI */
    @Column(name = "REQUEST_URI", nullable = false, length = 500)
    private String requestUri;

    /** HTTP 메서드 (GET, POST, PUT, DELETE 등) */
    @Column(name = "HTTP_METHOD", nullable = false, length = 10)
    private String httpMethod;

    /** 로그 카테고리 (PAGE_VIEW, API_CALL, FILE_DOWNLOAD, AUTH_LOGIN, AUTH_LOGOUT, ERROR) */
    @Enumerated(EnumType.STRING)
    @Column(name = "LOG_CATEGORY", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'API_CALL'")
    private LogCategory logCategory;

    /** 클라이언트 IP 주소 */
    @Column(name = "CLIENT_IP", length = 50)
    private String clientIp;

    /** 사용자 ID (인증된 경우) */
    @Column(name = "USER_ID", length = 100)
    private String userId;

    /** 요청 파라미터 (JSON 형식) - Query String 및 Path Variable */
    @Column(name = "REQUEST_PARAMS", columnDefinition = "TEXT")
    private String requestParams;

    /** 요청 시간 */
    @Column(name = "REQUEST_TIME", nullable = false)
    private LocalDateTime requestTime;

    /** 응답 시간 */
    @Column(name = "RESPONSE_TIME")
    private LocalDateTime responseTime;

    /** 실행 시간 (밀리초) */
    @Column(name = "EXECUTION_TIME_MS")
    private Integer executionTimeMs;

    /** HTTP 상태 코드 */
    @Column(name = "STATUS_CODE")
    private Integer statusCode;

    /** 에러 메시지 (예외 발생 시) */
    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;
}

package app.backend.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시스템 로그 카테고리
 *
 * <p>API 접근 로그의 유형을 구분합니다.
 *
 * <p><strong>카테고리 분류 기준:</strong>
 *
 * <ul>
 *   <li>PAGE_VIEW: GET 요청 중 페이지 조회 (API가 아닌 경우)
 *   <li>API_CALL: REST API 호출 (/api/** 경로)
 *   <li>FILE_DOWNLOAD: 파일 다운로드 요청
 *   <li>AUTH_LOGIN: 로그인 요청
 *   <li>AUTH_LOGOUT: 로그아웃 요청
 *   <li>ERROR: 에러 응답 (4xx, 5xx)
 * </ul>
 *
 * <p><strong>자동 분류 로직:</strong>
 *
 * <ol>
 *   <li>STATUS_CODE >= 400 → ERROR
 *   <li>URI contains "/login" → AUTH_LOGIN
 *   <li>URI contains "/logout" → AUTH_LOGOUT
 *   <li>URI contains "/download" or "/files/" → FILE_DOWNLOAD
 *   <li>URI starts with "/api/" → API_CALL
 *   <li>GET method and not API → PAGE_VIEW
 *   <li>기타 → API_CALL (기본값)
 * </ol>
 *
 * @see app.backend.core.entity.SysLog
 * @see app.backend.core.aspect.DatabaseLoggingAspect
 */
@Getter
@RequiredArgsConstructor
public enum LogCategory {
    /** 페이지 조회 */
    PAGE_VIEW("페이지 조회"),

    /** API 호출 */
    API_CALL("API 호출"),

    /** 파일 다운로드 */
    FILE_DOWNLOAD("파일 다운로드"),

    /** 로그인 */
    AUTH_LOGIN("로그인"),

    /** 로그아웃 */
    AUTH_LOGOUT("로그아웃"),

    /** 에러 */
    ERROR("에러");

    private final String description;

    /**
     * 요청 정보를 기반으로 로그 카테고리를 자동 분류
     *
     * @param requestUri 요청 URI
     * @param httpMethod HTTP 메서드
     * @param statusCode HTTP 상태 코드
     * @return 분류된 로그 카테고리
     */
    public static LogCategory classify(String requestUri, String httpMethod, Integer statusCode) {
        // 1. 에러 응답
        if (statusCode != null && statusCode >= 400) {
            return ERROR;
        }

        // 2. 인증 관련
        if (requestUri.contains("/login")) {
            return AUTH_LOGIN;
        }
        if (requestUri.contains("/logout")) {
            return AUTH_LOGOUT;
        }

        // 3. 파일 다운로드
        if (requestUri.contains("/download") || requestUri.contains("/files/")) {
            return FILE_DOWNLOAD;
        }

        // 4. API 호출
        if (requestUri.startsWith("/api/")) {
            return API_CALL;
        }

        // 5. 페이지 조회 (GET이면서 API가 아닌 경우)
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return PAGE_VIEW;
        }

        // 6. 기본값
        return API_CALL;
    }
}

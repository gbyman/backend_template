package app.backend.core.utils;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * IP 주소 추출 유틸리티
 *
 * <p>다양한 프록시 및 로드밸런서 환경에서 클라이언트의 실제 IP 주소를 추출합니다.
 *
 * <p><strong>지원하는 프록시 헤더:</strong>
 *
 * <ul>
 *   <li>X-Forwarded-For (일반적인 프록시)
 *   <li>Proxy-Client-IP (Apache 서버 프록시)
 *   <li>WL-Proxy-Client-IP (WebLogic 프록시)
 *   <li>HTTP_CLIENT_IP (일부 프록시)
 *   <li>HTTP_X_FORWARDED_FOR (일부 프록시)
 *   <li>X-Real-IP (nginx)
 *   <li>X-RealIP (대소문자 변형)
 *   <li>REMOTE_ADDR (일부 프록시)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * String clientIp = IpUtils.getClientIp(request);
 * log.info("Client IP: {}", clientIp);
 * </pre>
 */
@UtilityClass
public class IpUtils {

    /**
     * 체크할 프록시 헤더 목록 (우선순위 순서)
     *
     * <p>우선순위가 높은 헤더부터 체크하여 첫 번째로 유효한 IP를 반환합니다.
     */
    private static final String[] PROXY_HEADERS = {
        "X-Forwarded-For", // 가장 일반적인 프록시 헤더
        "Proxy-Client-IP", // Apache 서버 프록시
        "WL-Proxy-Client-IP", // WebLogic 프록시
        "HTTP_CLIENT_IP", // 일부 프록시 서버
        "HTTP_X_FORWARDED_FOR", // 일부 프록시 서버
        "X-Real-IP", // nginx 프록시
        "X-RealIP", // 대소문자 변형
        "REMOTE_ADDR" // 일부 프록시 서버
    };

    /**
     * HTTP 요청에서 클라이언트의 실제 IP 주소를 추출합니다.
     *
     * <p>프록시 및 로드밸런서를 거쳐온 요청의 경우 다양한 헤더를 순차적으로 확인하여 원본 클라이언트 IP를 추출합니다.
     *
     * <p><strong>헤더 검증 조건:</strong>
     *
     * <ul>
     *   <li>null이 아니어야 함
     *   <li>빈 문자열이 아니어야 함
     *   <li>"unknown" 문자열이 아니어야 함 (대소문자 구분 없음)
     * </ul>
     *
     * <p><strong>X-Forwarded-For 특별 처리:</strong>
     *
     * <pre>
     * X-Forwarded-For: client, proxy1, proxy2
     * → 첫 번째 IP(client)를 반환
     * </pre>
     *
     * @param request HttpServletRequest 객체
     * @return 클라이언트 IP 주소 (IPv4 또는 IPv6)
     */
    public String getClientIp(HttpServletRequest request) {
        // 다양한 프록시 헤더를 순차적으로 확인
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);

            if (isValidIp(ip)) {
                // X-Forwarded-For는 콤마로 구분된 IP 리스트일 수 있음
                // 형식: "client, proxy1, proxy2"
                // 첫 번째 IP가 원본 클라이언트 IP
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 모든 헤더에서 IP를 찾지 못한 경우 RemoteAddr 사용
        String ip = request.getRemoteAddr();

        // IPv6 로컬호스트를 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    /**
     * IP 주소가 유효한지 검증합니다.
     *
     * <p>다음 조건을 모두 만족해야 유효합니다:
     *
     * <ul>
     *   <li>null이 아님
     *   <li>빈 문자열이 아님
     *   <li>"unknown" 문자열이 아님 (대소문자 구분 없음)
     * </ul>
     *
     * @param ip 검증할 IP 주소 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    private boolean isValidIp(String ip) {
        return StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}

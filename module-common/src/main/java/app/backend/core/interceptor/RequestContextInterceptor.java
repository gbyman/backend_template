package app.backend.core.interceptor;

import java.net.InetAddress;

import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import app.backend.core.base.vo.ReqContextVo;
import app.backend.core.constants.RequestAttributeKey;
import app.backend.core.property.I18nProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청 컨텍스트 및 MDC 로깅 인터셉터
 *
 * <p>모든 HTTP 요청에 대해 다음 작업을 수행합니다:
 *
 * <ul>
 *   <li>ReqContextVo 생성 및 request attribute에 저장
 *   <li>MDC(Mapped Diagnostic Context)에 요청 컨텍스트 정보 등록
 *   <li>요청 완료 후 MDC 정리 (메모리 누수 방지)
 * </ul>
 *
 * <p><strong>MDC에 등록되는 정보:</strong>
 *
 * <ul>
 *   <li>requestId - 요청 고유 ID (UUID)
 *   <li>hostName - 서버 호스트명
 *   <li>serverIp - 서버 IP 주소
 *   <li>clientIp - 클라이언트 IP 주소
 *   <li>httpMethod - HTTP 메서드 (GET, POST 등)
 *   <li>requestUri - 요청 URI
 *   <li>langDivVal - 언어 구분 값 (ko, en 등)
 * </ul>
 *
 * <p><strong>Logback 설정 예시:</strong>
 *
 * <pre>
 * &lt;pattern&gt;
 *   %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}
 *   [%X{requestId}] [%X{clientIp}] [%X{httpMethod}] - %msg%n
 * &lt;/pattern&gt;
 * </pre>
 *
 * <p><strong>로그 출력 예시:</strong>
 *
 * <pre>
 * 2026-02-16 12:34:56.789 [http-nio-8080-exec-1] INFO  c.e.c.UserController
 * [a1b2c3d4-e5f6-7890-abcd-ef1234567890] [192.168.1.100] [POST] - User created: userId=123
 * </pre>
 *
 * @see org.slf4j.MDC
 */
@Slf4j
@RequiredArgsConstructor
public class RequestContextInterceptor implements HandlerInterceptor {

    private final I18nProperties i18nProperties;

    /**
     * 요청 처리 전 ReqContextVo 생성 및 MDC 설정
     *
     * <p>다음 작업을 수행합니다:
     *
     * <ol>
     *   <li>ReqContextVo 생성 (requestId, clientIp, httpMethod 등)
     *   <li>Request attribute에 ReqContextVo 저장
     *   <li>MDC에 요청 컨텍스트 정보 등록
     * </ol>
     *
     * <p><strong>MDC 등록 실패 시:</strong>
     *
     * <ul>
     *   <li>경고 로그 출력
     *   <li>요청 처리는 계속 진행 (MDC 설정 실패가 요청을 차단하지 않음)
     * </ul>
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param handler 핸들러 (컨트롤러 메서드)
     * @return 항상 true (요청 처리 계속 진행)
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        ReqContextVo contextVo =
                ReqContextVo.from(
                        request,
                        i18nProperties.getDefaultLang(),
                        i18nProperties.getSupportedLangs());

        request.setAttribute(RequestAttributeKey.REQUEST_CONTEXT.name(), contextVo);

        log.debug(">>>>> Request Context: {}", contextVo);

        // MDC에 요청 컨텍스트 정보 등록
        setupMdc(contextVo);

        return true;
    }

    /**
     * 요청 완료 후 MDC 정리
     *
     * <p><strong>중요:</strong> MDC는 ThreadLocal을 사용하므로 반드시 정리해야 합니다. 정리하지 않으면 Thread Pool 재사용 시 이전
     * 요청의 MDC 정보가 남아있어 로그가 오염될 수 있습니다.
     *
     * <p>이 메서드는 요청 처리 성공/실패 여부와 관계없이 항상 호출됩니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param handler 핸들러 (컨트롤러 메서드)
     * @param ex 예외 (있는 경우)
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        // MDC 정리 (ThreadLocal 메모리 누수 방지)
        MDC.clear();

        log.debug("<<<<< Request completed: {} {}", request.getMethod(), request.getRequestURI());
    }

    /**
     * MDC에 요청 컨텍스트 정보를 등록합니다.
     *
     * <p>등록되는 정보:
     *
     * <ul>
     *   <li>requestId - 요청 고유 ID
     *   <li>hostName - 서버 호스트명
     *   <li>serverIp - 서버 IP 주소
     *   <li>clientIp - 클라이언트 IP 주소
     *   <li>httpMethod - HTTP 메서드
     *   <li>requestUri - 요청 URI
     *   <li>langDivVal - 언어 구분 값
     * </ul>
     *
     * <p>서버 정보(hostName, serverIp) 조회 실패 시 경고 로그를 출력하고 계속 진행합니다.
     *
     * @param contextVo 요청 컨텍스트 VO
     */
    private void setupMdc(ReqContextVo contextVo) {
        try {
            // 서버 정보 조회
            InetAddress inetAddress = InetAddress.getLocalHost();

            // MDC에 요청 컨텍스트 정보 등록
            MDC.put("requestId", contextVo.getRequestId());
            MDC.put("hostName", inetAddress.getHostName());
            MDC.put("serverIp", inetAddress.getHostAddress());
            MDC.put("clientIp", contextVo.getClientIp());
            MDC.put("httpMethod", contextVo.getHttpMethod().name());
            MDC.put("requestUri", contextVo.getRequestUri());
            MDC.put("langDivVal", contextVo.getLangDivVal());

            log.debug(
                    "MDC setup completed - requestId: {}, clientIp: {}, method: {} {}",
                    contextVo.getRequestId(),
                    contextVo.getClientIp(),
                    contextVo.getHttpMethod(),
                    contextVo.getRequestUri());

        } catch (Exception e) {
            // MDC 설정 실패는 요청 처리를 막지 않음
            log.warn("Failed to setup MDC context", e);
        }
    }
}

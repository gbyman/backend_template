package app.backend.common.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 요청 본문을 여러 번 읽을 수 있도록 버퍼링하는 래퍼 클래스
 *
 * <p>일반적으로 HttpServletRequest의 InputStream은 한 번만 읽을 수 있습니다. 이 클래스는 요청 본문을 메모리에 버퍼링하여 여러 번 읽을 수 있도록
 * 합니다.
 *
 * <p><strong>사용 사례:</strong>
 *
 * <ul>
 *   <li>요청 로깅: 요청 본문을 로깅한 후 컨트롤러에서 다시 읽어야 할 때
 *   <li>요청 검증: Filter에서 요청 본문을 검증한 후 실제 처리에서 다시 읽어야 할 때
 *   <li>요청 암호화/복호화: 요청 본문을 복호화한 후 다시 읽어야 할 때
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // Filter에서 사용
 * public class RequestLoggingFilter implements Filter {
 *     &#64;Override
 *     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
 *             throws IOException, ServletException {
 *
 *         if (request instanceof HttpServletRequest httpRequest) {
 *             ReadableRequestWrapper wrapper = new ReadableRequestWrapper(httpRequest);
 *
 *             // 첫 번째 읽기: 로깅용
 *             String body = wrapper.getRequestBody();
 *             log.info("Request body: {}", body);
 *
 *             // 두 번째 읽기: 실제 처리 (Controller에서)
 *             chain.doFilter(wrapper, response);
 *         } else {
 *             chain.doFilter(request, response);
 *         }
 *     }
 * }
 *
 * // Controller에서 정상적으로 @RequestBody 사용 가능
 * &#64;PostMapping("/api/users")
 * public ResponseEntity&lt;UserDto&gt; createUser(&#64;RequestBody UserDto userDto) {
 *     // wrapper를 통해 전달된 요청 본문을 정상적으로 읽을 수 있음
 *     return ResponseEntity.ok(userService.createUser(userDto));
 * }
 * </pre>
 *
 * <p><strong>⚠️ 주의사항:</strong>
 *
 * <ul>
 *   <li>요청 본문을 메모리에 버퍼링하므로 대용량 요청 시 메모리 부족 가능
 *   <li>파일 업로드 등 대용량 요청에는 사용하지 마세요
 *   <li>필요한 경우에만 사용하여 성능 오버헤드를 최소화하세요
 * </ul>
 */
@Slf4j
public class ReadableRequestWrapper extends HttpServletRequestWrapper {

    /** 버퍼링된 요청 본문 데이터 */
    private final byte[] rawData;

    /** 요청 문자 인코딩 */
    private final Charset encoding;

    /**
     * ReadableRequestWrapper 생성자
     *
     * @param request 원본 HTTP 요청
     * @throws IOException 요청 본문 읽기 실패 시
     */
    public ReadableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // 인코딩 결정 (기본값: UTF-8)
        String characterEncoding = request.getCharacterEncoding();
        this.encoding =
                (characterEncoding != null && !characterEncoding.isBlank())
                        ? Charset.forName(characterEncoding)
                        : StandardCharsets.UTF_8;

        // 요청 본문을 byte array로 읽어서 저장
        try (InputStream inputStream = request.getInputStream()) {
            this.rawData = inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("Failed to read request body", e);
            throw e;
        }
    }

    /**
     * 버퍼링된 데이터로부터 새로운 ServletInputStream 반환
     *
     * @return 버퍼링된 데이터를 읽을 수 있는 ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rawData);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("ReadListener is not supported");
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    /**
     * 버퍼링된 데이터로부터 BufferedReader 반환
     *
     * @return 버퍼링된 데이터를 읽을 수 있는 BufferedReader
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), encoding));
    }

    /**
     * 요청 본문을 문자열로 반환
     *
     * <p>로깅이나 검증 목적으로 요청 본문 전체를 문자열로 가져올 때 사용합니다.
     *
     * @return 요청 본문 문자열
     */
    public String getRequestBody() {
        return new String(rawData, encoding);
    }

    /**
     * 버퍼링된 원본 바이트 배열 반환
     *
     * @return 요청 본문 바이트 배열 (방어적 복사본)
     */
    public byte[] getRawData() {
        return rawData.clone();
    }
}

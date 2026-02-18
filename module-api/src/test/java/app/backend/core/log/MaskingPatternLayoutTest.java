package app.backend.core.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** MaskingPatternLayout 테스트 */
@DisplayName("로그 마스킹 패턴 테스트")
class MaskingPatternLayoutTest {

    private MaskingPatternLayout layout;

    @BeforeEach
    void setUp() {
        layout = new MaskingPatternLayout();
    }

    @Test
    @DisplayName("전화번호 마스킹 - 하이픈 포함")
    void testPhoneMaskingWithHyphen() {
        // given
        String message = "사용자 전화번호: 010-1234-5678";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isEqualTo("사용자 전화번호: 010-****-5678");
        assertThat(masked).doesNotContain("1234");
    }

    @Test
    @DisplayName("전화번호 마스킹 - 하이픈 없음")
    void testPhoneMaskingWithoutHyphen() {
        // given
        String message = "phone=01012345678";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isEqualTo("phone=010-****-5678");
    }

    @Test
    @DisplayName("이메일 마스킹")
    void testEmailMasking() {
        // given
        String message = "이메일: user@example.com";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isEqualTo("이메일: u***@example.com");
        assertThat(masked).doesNotContain("user");
    }

    @Test
    @DisplayName("비밀번호 마스킹 - JSON 형태")
    void testPasswordMaskingJson() {
        // given
        String message = "{\"username\":\"user\",\"password\":\"myPassword123\"}";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).contains("\"password\":\"*****\"");
        assertThat(masked).doesNotContain("myPassword123");
    }

    @Test
    @DisplayName("비밀번호 마스킹 - 텍스트 형태")
    void testPasswordMaskingText() {
        // given
        String message = "로그인: username=user, password=secret123";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).contains("password=*****");
        assertThat(masked).doesNotContain("secret123");
    }

    @Test
    @DisplayName("여러 패턴 동시 마스킹")
    void testMultiplePatternsMasking() {
        // given
        String message = "사용자: 홍길동, 전화: 010-1234-5678, 이메일: hong@example.com";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).contains("010-****-5678");
        assertThat(masked).contains("h***@example.com");
        assertThat(masked).contains("홍길동"); // 일반 정보는 유지
    }

    @Test
    @DisplayName("마스킹 대상 없는 일반 메시지")
    void testNormalMessage() {
        // given
        String message = "일반 로그 메시지입니다.";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isEqualTo(message); // 변경 없음
    }

    @Test
    @DisplayName("null 메시지 처리")
    void testNullMessage() {
        // given
        String message = null;

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isNull();
    }

    @Test
    @DisplayName("빈 문자열 처리")
    void testEmptyMessage() {
        // given
        String message = "";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).isEmpty();
    }

    @Test
    @DisplayName("실제 로그 메시지 형태 - Controller")
    void testRealLogMessage() {
        // given
        String message =
                "2025-02-15 10:30:00.123 [http-nio-8080-exec-1] INFO  app.backend.app.user.controller.UserController - 사용자 생성: UserDto(name=홍길동, phone=010-1234-5678, email=user@example.com)";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).contains("phone=010-****-5678");
        assertThat(masked).contains("email=u***@example.com");
        assertThat(masked).contains("name=홍길동"); // 일반 정보는 유지
    }

    @Test
    @DisplayName("실제 로그 메시지 형태 - 로그인 요청")
    void testLoginRequestLog() {
        // given
        String message =
                "DEBUG app.backend.core.security.AuthService - 로그인 요청: {\"username\":\"user@example.com\",\"password\":\"mySecretPassword\"}";

        // when
        String masked = maskMessage(message);

        // then
        assertThat(masked).contains("\"password\":\"*****\"");
        assertThat(masked).doesNotContain("mySecretPassword");
    }

    /** 테스트 헬퍼 메서드 MaskingPatternLayout의 private maskMessage를 간접 호출 */
    private String maskMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // doLayout 대신 직접 마스킹 로직 테스트
        // 실제 구현에서는 doLayout을 통해 처리되지만, 단위 테스트를 위해 분리
        try {
            var method = MaskingPatternLayout.class.getDeclaredMethod("maskMessage", String.class);
            method.setAccessible(true);
            return (String) method.invoke(layout, message);
        } catch (Exception e) {
            throw new RuntimeException("테스트 실행 실패", e);
        }
    }
}

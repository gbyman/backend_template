package app.backend.app.mlg.bundle.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import app.backend.app.mlg.group.service.MlgGroupService;
import app.backend.support.BaseControllerTest;

/*
 * @WebMvcTest 슬라이스 테스트 설정 가이드
 *
 * excludeAutoConfiguration:
 *   Spring Security 자동 설정을 제외합니다.
 *   포함되면 SecurityFilterChain이 활성화되어 모든 요청이 401을 반환합니다.
 *
 * excludeFilters:
 *   @WebMvcTest는 @Controller 외에 @Component(Filter, Interceptor),
 *   WebMvcConfigurer 등도 컴포넌트 스캔으로 로드합니다.
 *   아래 패키지의 빈들이 로드되면 의존성 충돌이 발생하여 제외합니다:
 *
 *   - config:    SecurityConfig → JwtAuthenticationFilter 등 JWT 빈 의존
 *   - jwt:       TokenProvider, JwtService → Redis, UserDetailsService 의존
 *   - ratelimit: RateLimitConfig ↔ RateLimitInterceptor 순환 참조
 *   - security:  CustomUserDetailsService → UserRepository 의존
 *
 *   제외해도 core.exception.GlobalExceptionHandler(@RestControllerAdvice)는
 *   정상 로드되어 BizRespVo 응답 포맷이 유지됩니다.
 */
@DisplayName("MlgBundleController 테스트")
@WebMvcTest(
        value = MlgBundleController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        },
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.backend\\.core\\.(config|jwt|ratelimit|security).*"))
class MlgBundleControllerTest extends BaseControllerTest {

    @MockitoBean private MlgGroupService mlgGroupService;

    private static final String BUNDLE_URL = "/api/v1/i18n/messages";

    @Test
    @DisplayName("GET /api/v1/i18n/messages?lang=ko - 번들 조회")
    void getBundleWithLangParam() throws Exception {
        // given
        Map<String, String> bundle = new LinkedHashMap<>();
        bundle.put("MLG0000001", "저장");
        bundle.put("MLG0000002", "취소");
        given(mlgGroupService.getBundle("ko")).willReturn(bundle);

        // when & then
        mockMvc.perform(get(BUNDLE_URL).param("lang", "ko"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.body.MLG0000001").value("저장"))
                .andExpect(jsonPath("$.body.MLG0000002").value("취소"));
    }

    @Test
    @DisplayName("GET /api/v1/i18n/messages?lang=en - 영어 번들")
    void getBundleEnglish() throws Exception {
        // given
        Map<String, String> bundle = new LinkedHashMap<>();
        bundle.put("MLG0000001", "Save");
        bundle.put("MLG0000002", "Cancel");
        given(mlgGroupService.getBundle("en")).willReturn(bundle);

        // when & then
        mockMvc.perform(get(BUNDLE_URL).param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.MLG0000001").value("Save"))
                .andExpect(jsonPath("$.body.MLG0000002").value("Cancel"));
    }

    @Test
    @DisplayName("GET /api/v1/i18n/messages - 빈 번들")
    void getBundleEmpty() throws Exception {
        // given
        given(mlgGroupService.getBundle("ja")).willReturn(new LinkedHashMap<>());

        // when & then
        mockMvc.perform(get(BUNDLE_URL).param("lang", "ja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").isEmpty());
    }
}

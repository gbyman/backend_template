package app.backend.app.mlg.group.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;
import app.backend.app.mlg.group.service.MlgGroupService;
import app.backend.support.BaseControllerTest;
import app.backend.support.TestFixtures;

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
@DisplayName("MlgGroupController 테스트")
@WebMvcTest(
        value = MlgGroupController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        },
        excludeFilters =
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "app\\.backend\\.core\\.(config|jwt|ratelimit|security).*"))
class MlgGroupControllerTest extends BaseControllerTest {

    @MockitoBean private MlgGroupService mlgGroupService;

    private static final String BASE_URL = "/api/v1/system/mlg";

    @Test
    @DisplayName("GET /api/v1/system/mlg - 목록 조회 200 OK")
    void paging() throws Exception {
        // given
        MlgPagingRespDto dto = new MlgPagingRespDto();
        Page<MlgPagingRespDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        given(mlgGroupService.paging(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/system/mlg/{mlgCodeVal} - 상세 조회 200 OK")
    void getGroup() throws Exception {
        // given
        MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000001");
        given(mlgGroupService.getGroup("MLG0000001")).willReturn(respDto);

        // when & then
        mockMvc.perform(get(BASE_URL + "/MLG0000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.body.mlgCodeVal").value("MLG0000001"))
                .andExpect(jsonPath("$.body.details").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/system/mlg - 등록 201 Created")
    void createGroup() throws Exception {
        // given
        MlgGroupReqDto.Create reqDto = TestFixtures.createMlgGroupReqCreate();
        MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000001");
        given(mlgGroupService.createGroup(any())).willReturn(respDto);

        // when & then
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(201))
                .andExpect(jsonPath("$.body.mlgCodeVal").value("MLG0000001"));
    }

    @Test
    @DisplayName("POST /api/v1/system/mlg - 유효성 실패 (details 빈 리스트)")
    void createGroupValidationFail() throws Exception {
        // given
        MlgGroupReqDto.Create reqDto = new MlgGroupReqDto.Create();
        reqDto.setDetails(List.of()); // @NotEmpty 위반

        // when & then
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(reqDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/system/mlg/{mlgCodeVal} - 수정 200 OK")
    void updateGroup() throws Exception {
        // given
        MlgGroupReqDto.Update reqDto = TestFixtures.createMlgGroupReqUpdate();
        MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000001");
        given(mlgGroupService.updateGroup(eq("MLG0000001"), any())).willReturn(respDto);

        // when & then
        mockMvc.perform(
                        put(BASE_URL + "/MLG0000001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200))
                .andExpect(jsonPath("$.body.mlgCodeVal").value("MLG0000001"));
    }

    @Test
    @DisplayName("DELETE /api/v1/system/mlg/{mlgCodeVal} - 삭제 200 OK")
    void deleteGroup() throws Exception {
        // given
        willDoNothing().given(mlgGroupService).deleteGroup("MLG0000001");

        // when & then
        mockMvc.perform(delete(BASE_URL + "/MLG0000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(200));
    }
}

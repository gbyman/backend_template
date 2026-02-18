package app.backend.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller 슬라이스 테스트 Base 클래스
 *
 * <p>MVC 관련 빈만 로드하여 빠른 테스트를 수행합니다. Security 자동 설정을 제외하여 JWT 필터 의존성 없이 테스트 가능합니다.
 *
 * <p>사용 시 서브클래스에서 &#64;WebMvcTest에 excludeAutoConfiguration으로 SecurityAutoConfiguration,
 * SecurityFilterAutoConfiguration을 지정하세요.
 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}

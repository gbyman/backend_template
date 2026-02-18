package app.backend.core.jpa.auditoraware;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import app.backend.core.utils.SecurityUtils;

/** JPA Auditing을 위한 AuditorAware 구현체 현재 사용자 정보를 자동으로 등록/수정자 필드에 주입합니다. */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            // SecurityUtils로 현재 로그인한 사용자 ID 가져오기
            String userId = SecurityUtils.getUserId();
            return Optional.of(userId != null ? userId : "SYSTEM");

        } catch (Exception e) {
            // 인증되지 않은 경우 SYSTEM 사용
            return Optional.of("SYSTEM");
        }
    }
}

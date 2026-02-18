package app.backend.core.security;

import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/**
 * 관리자 전용 권한 검증 매니저 특정 엔드포인트를 관리자만 접근 가능하도록 제한합니다.
 *
 * <p>사용 예시 (SecurityConfig):
 *
 * <pre>
 * http.authorizeHttpRequests(auth -> auth
 *     .requestMatchers("/api/admin/**").access(adminOnlyAuthorizationManager)
 * );
 * </pre>
 */
@Component
public class AdminOnlyAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {

        Authentication authentication = authenticationSupplier.get();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // TODO: CustomUserDetails 구현 후 관리자 여부 확인 로직 추가
        // Object principal = authentication.getPrincipal();
        // if (principal instanceof CustomUserDetails userDetails) {
        //     return new AuthorizationDecision(userDetails.isAdminYn());
        // }

        // 임시: ROLE_ADMIN 권한 체크
        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> role.equals("ROLE_ADMIN"));

        return new AuthorizationDecision(isAdmin);
    }
}

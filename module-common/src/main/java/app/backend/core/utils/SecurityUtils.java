package app.backend.core.utils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.experimental.UtilityClass;

/** Spring Security 유틸리티 현재 인증된 사용자 정보를 조회하는 공통 메서드를 제공합니다. */
@UtilityClass
public class SecurityUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    /** 현재 인증된 Authentication 객체를 반환합니다. 인증되지 않았거나 anonymousUser인 경우 AccessDeniedException 발생. */
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        return authentication;
    }

    /** 현재 인증된 사용자의 Principal 객체를 반환합니다. */
    public Object getPrincipal() {
        return getAuthentication().getPrincipal();
    }

    /**
     * 현재 인증된 사용자의 UserDetails를 반환합니다. principal이 UserDetails 타입이 아닌 경우 AccessDeniedException 발생.
     */
    public UserDetails getUserDetails() {
        Object principal = getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            throw new AccessDeniedException("인증 정보가 UserDetails 형식이 아닙니다.");
        }

        return userDetails;
    }

    /** 현재 로그인된 사용자의 username을 반환합니다. */
    public String getUsername() {
        return getUserDetails().getUsername();
    }

    /** 현재 로그인된 사용자의 ID를 반환합니다. (username과 동일한 값) */
    public String getUserId() {
        return getAuthentication().getName();
    }

    /** 현재 사용자가 특정 권한을 가지고 있는지 확인합니다. */
    public boolean hasAuthority(String authority) {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /** 현재 사용자가 특정 역할을 가지고 있는지 확인합니다. */
    public boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return hasAuthority(roleWithPrefix);
    }

    /** 현재 로그인된 사용자가 관리자인지 확인합니다. */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /** 현재 로그인된 사용자의 권한 목록을 반환합니다. */
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthentication().getAuthorities();
    }
}

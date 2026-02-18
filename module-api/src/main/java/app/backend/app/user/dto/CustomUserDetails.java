package app.backend.app.user.dto;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import app.backend.app.user.entity.UserEntity;
import lombok.Getter;

/** Spring Security UserDetails 구현체 UserEntity를 래핑하여 Spring Security의 인증/인가에 사용 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final UserEntity user;
    private final String role;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
        this.role = user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (필요시 로직 추가)
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (필요시 로직 추가)
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }

    /** UserEntity에 직접 접근할 수 있는 메서드 추가 정보가 필요할 때 사용 */
    public Long getUserId() {
        return user.getId();
    }

    public String getDisplayName() {
        return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }
}

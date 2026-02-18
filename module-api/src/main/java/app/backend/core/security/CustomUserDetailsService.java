package app.backend.core.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.app.user.dto.CustomUserDetails;
import app.backend.app.user.entity.UserEntity;
import app.backend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Spring Security UserDetailsService 구현체 JWT 인증 시 사용자 정보를 로드하는 서비스 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        UserEntity user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserDetails(user);
    }

    /** username 존재 여부 확인 */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /** email 존재 여부 확인 */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}

package app.backend.core.jwt.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import app.backend.core.base.exception.BizException;
import app.backend.core.constants.MessageConstants;
import app.backend.core.jwt.provider.TokenProvider;
import app.backend.core.jwt.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.public-uris}")
    private String[] publicUris;

    @Value("${jwt.use-redis:false}")
    private boolean useRedis;

    private final TokenProvider tokenProvider;

    private final JwtService jwtService;

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = tokenProvider.getAccessTokenFromHeader();

        if (StringUtils.isNotBlank(accessToken)) {

            validateJwt(accessToken);

            Authentication authentication = tokenProvider.getAuthentication(accessToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            log.debug("토큰 존재 x");
        }

        filterChain.doFilter(request, response);
    }

    private void validateJwt(String accessToken) {
        // Redis 사용 시에만 아래 검증 수행 (JwtService 내부에서 useRedis 체크)

        // accessToken blackList 존재 여부 확인 - 존재하면 재발급 필요
        // Redis 미사용 시: 항상 false 반환 (stateless)
        if (jwtService.isBlacklisted(accessToken)) {
            throw new ExpiredJwtException(null, null, "강제 만료된 accessToken 재발행 필요");
        }

        // refreshToken 다르면 중복 로그인
        // Redis 미사용 시: 항상 true 반환 (중복 로그인 체크 안함)
        if (!jwtService.isRefreshTokenMatched()) {
            jwtService.expireAccessToken();
            throw new BizException(HttpStatus.BAD_REQUEST, MessageConstants.DUPLICATE_LOGIN);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        for (String uri : publicUris) {
            if (ANT_PATH_MATCHER.match(uri, requestUri)) {
                return true;
            }
        }
        return false;
    }
}

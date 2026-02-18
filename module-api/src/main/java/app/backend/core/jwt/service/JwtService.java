package app.backend.core.jwt.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import app.backend.core.jwt.constants.ClaimKeys;
import app.backend.core.jwt.constants.JwtType;
import app.backend.core.jwt.domain.blacklist.service.AccessTokenBlacklistService;
import app.backend.core.jwt.domain.refreshtoken.entity.RefreshTokenEntity;
import app.backend.core.jwt.domain.refreshtoken.repository.RefreshTokenRepository;
import app.backend.core.jwt.dto.JwtDto;
import app.backend.core.jwt.provider.TokenProvider;
import app.backend.core.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {

    private static final long MILLIS_PER_SECOND = 1000;

    @Value("${allow-duplicate.ids}")
    private List<String> allowDuplicateLoginIds;

    @Value("${jwt.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Value("${jwt.use-redis:false}")
    private boolean useRedis;

    private static final String REFRESH_TOKEN_COOKIE_KEY = "templateRefresh";

    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenProvider tokenProvider;

    private final AccessTokenBlacklistService accessTokenBlacklistService;

    private final Environment environment;

    public JwtService(
            TokenProvider tokenProvider,
            Environment environment,
            @Autowired(required = false) RefreshTokenRepository refreshTokenRepository,
            @Autowired(required = false) AccessTokenBlacklistService accessTokenBlacklistService) {
        this.tokenProvider = tokenProvider;
        this.environment = environment;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenBlacklistService = accessTokenBlacklistService;
    }

    /**
     * AccessToken, RefreshToken 생성 <ui>
     * <li>Refresh Token 쿠키 저장
     * <li>Redis 사용 시 RefreshToken Redis 저장 </ui>
     *
     * @param userDetails UserDetails
     * @return JwtDto
     */
    public JwtDto generateToken(UserDetails userDetails) {
        // JWT 토큰 생성
        String accessToken = tokenProvider.createAccessToken(userDetails);
        String refreshToken = tokenProvider.createRefreshToken(userDetails.getUsername());

        storeRefreshToken(refreshToken, userDetails.getUsername());

        return new JwtDto(accessToken, refreshToken);
    }

    /**
     * Token 강제 만료 처리
     *
     * <ul>
     *   <li>accessToken 만료되지 않았으면 Redis BlackList에 추가
     *   <li>Login시 저장된 RefreshToken 정보 제거
     * </ul>
     */
    public void expireToken() {

        // refreshToken 만료 처리
        expireRefreshToken();

        // accessToken blackList 처리
        expireAccessToken();
    }

    public String getRequestAccessToken() {
        return tokenProvider.getAccessTokenFromHeader();
    }

    public String getRequestRefreshToken() {

        return CookieUtils.getCookie(REFRESH_TOKEN_COOKIE_KEY);
    }

    public void expireAccessToken() {
        if (!useRedis) {
            log.debug("Redis 미사용 모드: AccessToken 블랙리스트 처리 건너뜀");
            return;
        }

        String accessToken = getRequestAccessToken();

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        try {
            Date expiration = tokenProvider.parseClaimsFromJwt(accessToken).getExpiration();
            long now = System.currentTimeMillis(); // 현재 시간 (밀리초)
            long expirationSeconds = (expiration.getTime() - now) / MILLIS_PER_SECOND;

            // redis에AccessToken blackList에 추가
            accessTokenBlacklistService.setBlackList(accessToken, expirationSeconds);

        } catch (JwtException e) {
            // 만료 및 잘못된 토큰은 넘김
            log.error(">> AccessToken 만료 처리 {}", e.getMessage());
        }
    }

    public void expireRefreshToken() {
        String refreshToken = CookieUtils.getCookie(REFRESH_TOKEN_COOKIE_KEY);

        if (StringUtils.isBlank(refreshToken)) {
            return;
        }

        try {
            Claims claims = tokenProvider.parseClaimsFromJwt(refreshToken);

            validateExpireRefreshToken(claims);

            // 쿠키 제거
            CookieUtils.deleteCookie(REFRESH_TOKEN_COOKIE_KEY);

            // Redis 사용 시에만 RefreshToken 삭제
            if (useRedis) {
                String userId = claims.getSubject();
                Optional<RefreshTokenEntity> entityOpt = refreshTokenRepository.findById(userId);

                if (entityOpt.isPresent()
                        && refreshToken.equals(entityOpt.get().getRefreshToken())) {
                    refreshTokenRepository.deleteById(userId);
                }
            }

        } catch (ExpiredJwtException e) { // 이미 만료된 토큰이면 return
            log.debug(">>> 로그아웃 (이미 만료된 토큰)");
        }
    }

    private void validateExpireRefreshToken(Claims claims) {
        String tokenType =
                tokenProvider.getValueFromClaims(
                        claims, ClaimKeys.TOKEN_TYPE.getKey(), String.class);

        // jwt type refresh Token 인지 확인
        if (!JwtType.REFRESH.name().equals(tokenType)) {
            throw new IllegalArgumentException("RefreshToken으로 로그아웃 해야함");
        }
    }

    private void storeRefreshToken(String refreshToken, String userId) {
        Date expiration = tokenProvider.parseClaimsFromJwt(refreshToken).getExpiration();
        long remainingTime = expiration.getTime() - System.currentTimeMillis();

        // Redis 사용 시에만 Refresh Token을 Redis에 저장
        if (useRedis) {
            saveRefreshTokenInRedis(userId, refreshToken, remainingTime);
        }

        setRefreshTokenCookie(refreshToken, remainingTime / MILLIS_PER_SECOND);
    }

    /**
     * Refresh Token을 Redis에 저장
     *
     * @param userId 사용자 아이디
     * @param refreshToken refreshToken
     * @param remainingTime (ms)
     */
    private void saveRefreshTokenInRedis(String userId, String refreshToken, long remainingTime) {
        RefreshTokenEntity refreshTokenEntity =
                new RefreshTokenEntity(userId, refreshToken, remainingTime);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    /**
     * Refresh Token을 Cookie에 저장
     *
     * @param refreshToken refreshToken
     * @param remainingTime (s)
     */
    private void setRefreshTokenCookie(String refreshToken, long remainingTime) {
        // Refresh Token을 Cookie에 저장

        CookieUtils.setRefreshTokenCookie(
                REFRESH_TOKEN_COOKIE_KEY, refreshToken, remainingTime, refreshCookieSecure);
    }

    /** accessToken BlackList인지 확인 Redis 미사용 시 항상 false 반환 (stateless) */
    public boolean isBlacklisted(String accessToken) {
        if (!useRedis) {
            return false;
        }
        return accessTokenBlacklistService.isBlacklisted(accessToken);
    }

    /**
     * Client로 부터 넘어온 RefreshToken과 로그인 및 재발급시 저장한 refreshToken 을 비교 Redis 미사용 시 항상 true 반환
     * (stateless, 중복 로그인 체크 안함)
     *
     * @return 매치여부
     */
    public boolean isRefreshTokenMatched() {

        if (!useRedis) {
            log.debug("Redis 미사용 모드: RefreshToken 매칭 체크 건너뜀 (stateless)");
            return true;
        }

        if (environment.matchesProfiles("local")) {
            return true;
        }

        String refreshToken = getRequestRefreshToken();

        if (StringUtils.isBlank(refreshToken)) {
            throw new IllegalArgumentException();
        }

        String userId = getUserId(refreshToken);

        if (allowDuplicateLoginIds.contains(userId)) {
            return true; // 중복 로그인 허용 id
        }

        Optional<RefreshTokenEntity> refreshTokenOpt = refreshTokenRepository.findById(userId);

        if (refreshTokenOpt.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return refreshTokenOpt.get().getRefreshToken().equals(refreshToken);
    }

    public boolean isTokenValid(String token) {
        try {
            tokenProvider.parseClaimsFromJwt(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUserId(String jwtToken) {
        return tokenProvider.parseClaimsFromJwt(jwtToken).getSubject();
    }

    public String getAuthoritiesFromExpiredToken(String accessToken) {
        Claims claims = extractExpiredTokenClaims(accessToken);
        return tokenProvider.getValueFromClaims(
                claims, ClaimKeys.AUTH_CLAIM_KEY.getKey(), String.class);
    }

    private Claims extractExpiredTokenClaims(String token) {
        try {
            return tokenProvider.parseClaimsFromJwt(token);
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰에서도 claims를 가져옴
        }
    }
}

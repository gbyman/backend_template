package app.backend.core.jwt.provider;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import app.backend.core.jwt.constants.ClaimKeys;
import app.backend.core.jwt.constants.JwtType;
import app.backend.core.utils.ServletUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey key;
    private final Duration accessExpirationDuration;
    private final Duration refreshExpirationDuration;
    private final UserDetailsService userDetailsService;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-duration}") Duration accessExpirationDuration,
            @Value("${jwt.refresh-expiration-duration}") Duration refreshExpirationDuration,
            UserDetailsService userDetailsService) {

        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationDuration = accessExpirationDuration;
        this.refreshExpirationDuration = refreshExpirationDuration;
        this.userDetailsService = userDetailsService;
    }

    public String createAccessToken(UserDetails userDetails) {

        Claims claims = getAccessTokenClaims(userDetails);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .claims(claims)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String userId) {

        // 토큰의 expire 시간을 설정
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationDuration.toMillis());

        // Refresh Token 생성
        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .claims(Map.of(ClaimKeys.TOKEN_TYPE.getKey(), JwtType.REFRESH.name()))
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String jwtToken) {
        Claims claims = parseClaimsFromJwt(jwtToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    public Claims parseClaimsFromJwt(String jwtToken) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken).getPayload();
    }

    public String getAccessTokenFromHeader() {
        String bearerToken = ServletUtils.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()).trim();
        }

        return null;
    }

    private Claims getAccessTokenClaims(UserDetails userDetails) {
        String authorities =
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

        // 토큰의 expire 시간을 설정
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpirationDuration.toMillis());

        return Jwts.claims()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .add(ClaimKeys.AUTH_CLAIM_KEY.getKey(), authorities)
                .add(ClaimKeys.TOKEN_TYPE.getKey(), JwtType.ACCESS)
                .build();
    }

    public <T> T getValueFromClaims(Claims claims, String key, Class<T> responseType) {
        Object value = claims.get(key);

        if (value == null) {
            throw new IllegalArgumentException("The key '" + key + "' does not exist in claims.");
        }

        if (!responseType.isInstance(value)) {
            throw new ClassCastException(
                    "Cannot cast value of key '" + key + "' to " + responseType.getSimpleName());
        }

        return responseType.cast(value);
    }
}

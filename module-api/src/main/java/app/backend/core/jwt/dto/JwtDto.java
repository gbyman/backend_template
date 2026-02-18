package app.backend.core.jwt.dto;

/**
 * JWT 토큰 DTO (Access Token + Refresh Token).
 *
 * @param accessToken 액세스 토큰
 * @param refreshToken 리프레시 토큰
 */
public record JwtDto(String accessToken, String refreshToken) {}

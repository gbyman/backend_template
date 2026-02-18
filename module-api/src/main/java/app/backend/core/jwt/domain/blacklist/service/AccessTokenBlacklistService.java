package app.backend.core.jwt.domain.blacklist.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private final RedisTemplate<String, String> redisBlackListTemplate;

    // 블랙리스트에 추가 (key: "blacklist:token:{accessToken}", value: "invalid")
    public void setBlackList(String accessToken, Long expirationSeconds) {
        Objects.requireNonNull(accessToken, "accessToken 은 null일 수 없습니다.");
        Objects.requireNonNull(expirationSeconds, "expirationSeconds 는 null일 수 없습니다.");

        redisBlackListTemplate
                .opsForValue()
                .set(getKey(accessToken), "invalid", expirationSeconds, TimeUnit.SECONDS);
    }

    // 블랙리스트 조회
    public boolean isBlacklisted(String accessToken) {

        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(getKey(accessToken)));
    }

    // 블랙리스트에서 삭제 (필요한 경우)
    public boolean removeBlackList(String accessToken) {
        return Boolean.TRUE.equals(redisBlackListTemplate.delete(getKey(accessToken)));
    }

    // Key 생성 메서드
    private String getKey(String accessToken) {
        return "blacklist:token:" + accessToken;
    }
}

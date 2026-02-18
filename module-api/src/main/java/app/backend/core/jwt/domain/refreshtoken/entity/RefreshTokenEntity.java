package app.backend.core.jwt.domain.refreshtoken.entity;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RedisHash("refreshToken")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenEntity {
    @Id private String userId;
    private String refreshToken;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long timeToLive; // TTL (Time-To-Live) 설정, MILLISECONDS 단위

    public RefreshTokenEntity(String userId, String refreshToken, Long timeToLive) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.timeToLive = timeToLive;
    }
}

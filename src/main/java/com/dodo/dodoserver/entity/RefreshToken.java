package com.dodo.dodoserver.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * 리프레시 토큰을 Redis에 저장하기 위한 엔티티입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 604800) // 7일 후 자동 만료 (초 단위)
public class RefreshToken {

    @Id
    private String email; // 사용자 이메일을 Key로 사용합니다.

    @Indexed
    private String token; // 토큰 값 자체로 검색하기 위해 @Indexed를 추가합니다.

    /**
     * 재발급(RTR) 시 토큰 값을 새 값으로 교체합니다.
     */
    public void updateToken(String newToken) {
        this.token = newToken;
    }
}

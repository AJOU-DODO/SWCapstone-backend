package com.dodo.dodoserver.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private boolean isOnboarded; // 추가: 온보딩 완료 여부

    public static TokenResponseDto of(String accessToken, String refreshToken, Long expiresIn, boolean isOnboarded) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(expiresIn)
                .isOnboarded(isOnboarded)
                .build();
    }
}

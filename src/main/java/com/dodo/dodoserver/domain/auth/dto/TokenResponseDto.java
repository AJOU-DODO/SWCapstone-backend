package com.dodo.dodoserver.domain.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private boolean isOnboarded; // 추가: 온보딩 완료 여부
    private String role; // 추가: 유저 권한 (USER, ADMIN)

    public static TokenResponseDto of(String accessToken, String refreshToken, Long expiresIn, boolean isOnboarded, String role) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(expiresIn)
                .isOnboarded(isOnboarded)
                .role(role)
                .build();
    }
}

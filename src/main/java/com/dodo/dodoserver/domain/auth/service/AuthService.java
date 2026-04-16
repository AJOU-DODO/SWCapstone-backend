package com.dodo.dodoserver.domain.auth.service;

import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.domain.auth.entity.RefreshToken;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.auth.dao.RefreshTokenRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직(재발급, 로그아웃 등)을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    /**
     * Refresh Token을 확인하여 새로운 Access/Refresh 토큰 세트를 발급 (RTR 방식).
     */
    @Transactional
    public TokenResponseDto reissue(String refreshToken) {
        // 전달받은 Refresh Token의 유효성(서명, 만료시간 등)을 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Redis에 해당 토큰이 실제로 존재하는지 확인(보안 강화).
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 토큰의 주인(User)이 존재하는지 확인
        User user = userRepository.findByEmail(storedToken.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 새로운 토큰 쌍(Access, Refresh)을 생성 (Refresh Token Rotation - RTR).
        String newAccessToken = tokenProvider.createAccessToken(user.getEmail(), user.getRole().getKey());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        // Redis의 기존 토큰 정보를 새로운 토큰으로 업데이트
        storedToken.updateToken(newRefreshToken);
        refreshTokenRepository.save(storedToken);

        // 새로 발급된 토큰들을 클라이언트에 반환
        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(tokenProvider.getAccessTokenExpiration())
                .isOnboarded(user.isOnboarded())
                .build();
    }

    /**
     * 사용자의 로그아웃 요청 시 Redis에서 리프레시 토큰을 삭제
     */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    /**
     * 리프레시 토큰을 Redis에 저장하거나 기존 토큰을 갱신
     */
    @Transactional
    public void saveRefreshToken(String email, String token) {
        refreshTokenRepository.save(RefreshToken.builder()
                .email(email)
                .token(token)
                .build());
    }
}

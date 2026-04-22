package com.dodo.dodoserver.domain.auth.service;

import com.dodo.dodoserver.domain.auth.dao.RefreshTokenRepository;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.domain.auth.entity.RefreshToken;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("토큰 재발급 성공 - isOnboarded 상태 포함")
    void reissue_success() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String email = "test@example.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .role(Role.USER)
                .isOnboarded(true)
                .build();
        RefreshToken storedToken = RefreshToken.builder()
                .userId(1L)
                .token(oldRefreshToken)
                .build();

        given(tokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(oldRefreshToken)).willReturn(Optional.of(storedToken));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(tokenProvider.createAccessToken(user.getId(), email, user.getRole().getKey())).willReturn(newAccessToken);
        given(tokenProvider.createRefreshToken(email)).willReturn(newRefreshToken);
        given(tokenProvider.getAccessTokenExpiration()).willReturn(3600L);

        // when
        TokenResponseDto response = authService.reissue(oldRefreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(response.isOnboarded()).isTrue();
        verify(refreshTokenRepository, times(1)).save(storedToken);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void reissue_fail_invalidToken() {
        // given
        String invalidToken = "invalid-token";
        given(tokenProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.reissue(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 존재하지 않는 리프레시 토큰")
    void reissue_fail_tokenNotFound() {
        // given
        String refreshToken = "not-found-token";
        given(tokenProvider.validateToken(refreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // given
        Long userId = 1L;

        // when
        authService.logout(userId);

        // then
        verify(refreshTokenRepository, times(1)).deleteById(userId);
    }
}

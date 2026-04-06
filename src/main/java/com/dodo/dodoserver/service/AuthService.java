package com.dodo.dodoserver.service;

import com.dodo.dodoserver.entity.RefreshToken;
import com.dodo.dodoserver.entity.User;
import com.dodo.dodoserver.repository.RefreshTokenRepository;
import com.dodo.dodoserver.repository.UserRepository;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

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
     * Refresh Token을 확인하여 새로운 Access/Refresh 토큰 세트를 발급합니다 (RTR 방식).
     */
    @Transactional
    public Map<String, String> reissue(String refreshToken) {
        // 1. 전달받은 Refresh Token의 유효성(서명, 만료시간 등)을 검증합니다.
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Redis에 해당 토큰이 실제로 존재하는지 확인합니다 (보안 강화).
        // 존재하지 않는다면 이미 로그아웃했거나 탈취된 토큰일 가능성이 있습니다.
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("DB에 존재하지 않는 토큰입니다. 다시 로그인해주세요."));

        // 3. 토큰의 주인(User)이 존재하는지 확인합니다.
        User user = userRepository.findByEmail(storedToken.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 4. 새로운 토큰 쌍(Access, Refresh)을 생성합니다 (Refresh Token Rotation - RTR).
        String newAccessToken = tokenProvider.createAccessToken(user.getEmail(), user.getRole().getKey());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        // 5. Redis의 기존 토큰 정보를 새로운 토큰으로 업데이트합니다.
        storedToken.updateToken(newRefreshToken);
        refreshTokenRepository.save(storedToken);

        // 6. 새로 발급된 토큰들을 클라이언트에 반환합니다.
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }

    /**
     * 사용자의 로그아웃 요청 시 Redis에서 리프레시 토큰을 삭제하여 더 이상 재발급이 안 되도록 합니다.
     */
    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    /**
     * 리프레시 토큰을 Redis에 저장하거나 기존 토큰을 갱신합니다.
     */
    @Transactional
    public void saveRefreshToken(String email, String token) {
        refreshTokenRepository.save(RefreshToken.builder()
                .email(email)
                .token(token)
                .build());
    }
}

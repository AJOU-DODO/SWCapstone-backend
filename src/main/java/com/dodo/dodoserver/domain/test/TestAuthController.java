package com.dodo.dodoserver.domain.test;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
// @Profile({"local", "dev"})
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 특정 이메일을 기반으로 테스트용 Access Token과 Refresh Token을 생성
     * @param email 토큰에 담을 사용자 이메일 (DB에 없어도 생성 가능)
     * @param role 사용자 권한 (기본값: ROLE_USER)
     */
    @GetMapping("/token")
    public ApiResponseDto<TokenResponseDto> createTestToken(
            @RequestParam String email,
            @RequestParam(defaultValue = "ROLE_USER") String role) {

        String accessToken = jwtTokenProvider.createAccessToken(email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 만료 시간은 임의로 1시간(3600L)으로 표시
        return ApiResponseDto.success(TokenResponseDto.of(accessToken, refreshToken, 3600L, false));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Dodo! API is working.";
    }
}

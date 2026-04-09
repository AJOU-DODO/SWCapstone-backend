package com.dodo.dodoserver.domain.auth.controller;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 및 토큰 관리 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Access Token이 만료되었을 때, Refresh Token을 사용하여 새로운 토큰 쌍을 요청하는 엔드포인트
     * 비로그인(인증 없이) 상태에서도 접근 가능하도록 SecurityConfig에 설정
     */
    @PostMapping("/reissue")
    public ApiResponseDto<Map<String, String>> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ApiResponseDto.success(authService.reissue(refreshToken));
    }

    /**
     * 현재 로그인한 사용자의 세션을 만료(토큰 삭제)시키는 엔드포인트
     * 인증 필터를 거친 후 SecurityContext에 담긴 Authentication 객체에서 이메일을 추출
     */
    @PostMapping("/logout")
    public ApiResponseDto<String> logout(Authentication authentication) {
        // JwtAuthenticationFilter에서 저장한 인증 정보 중 이메일(Principal)을 가져옵니다.
        String email = (String) authentication.getPrincipal();
        authService.logout(email);
        return ApiResponseDto.success("성공적으로 로그아웃되었습니다.");
    }
}

package com.dodo.dodoserver.controller;

import com.dodo.dodoserver.dto.ApiResponse;
import com.dodo.dodoserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 및 토큰 관리 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Access Token이 만료되었을 때, Refresh Token을 사용하여 새로운 토큰 쌍을 요청하는 엔드포인트입니다.
     * 비로그인(인증 없이) 상태에서도 접근 가능하도록 SecurityConfig에 설정되어 있습니다.
     */
    @PostMapping("/reissue")
    public ApiResponse<Map<String, String>> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ApiResponse.success(authService.reissue(refreshToken));
    }

    /**
     * 현재 로그인한 사용자의 세션을 만료(토큰 삭제)시키는 엔드포인트입니다.
     * 인증 필터를 거친 후 SecurityContext에 담긴 Authentication 객체에서 이메일을 추출합니다.
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(Authentication authentication) {
        // JwtAuthenticationFilter에서 저장한 인증 정보 중 이메일(Principal)을 가져옵니다.
        String email = (String) authentication.getPrincipal();
        authService.logout(email);
        return ApiResponse.success("성공적으로 로그아웃되었습니다.");
    }
}

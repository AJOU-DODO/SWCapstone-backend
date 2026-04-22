package com.dodo.dodoserver.domain.auth.controller;

import com.dodo.dodoserver.domain.auth.dto.TokenReissueRequestDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dodo.dodoserver.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 및 토큰 관리 API 요청 처리 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Access Token 만료 시 Refresh Token을 통한 새로운 토큰 쌍 요청 엔드포인트
     * 비로그인 상태에서도 접근 가능하도록 SecurityConfig 설정
     */
    @PostMapping("/reissue")
    public ApiResponseDto<TokenResponseDto> reissue(@Valid @RequestBody TokenReissueRequestDto reissueRequestDto) {
        return ApiResponseDto.success(authService.reissue(reissueRequestDto.getRefreshToken()));
    }

    /**
     * 현재 로그인 사용자의 세션 만료(토큰 삭제) 엔드포인트
     * 인증 필터 통과 후 SecurityContext 내 UserPrincipal 객체에서 이메일 추출
     */
    @PostMapping("/logout")
    public ApiResponseDto<String> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getEmail());
        return ApiResponseDto.success("성공적으로 로그아웃되었습니다.");
    }
}

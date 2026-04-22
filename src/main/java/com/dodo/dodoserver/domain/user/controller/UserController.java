package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.domain.user.dto.UserProfileResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.user.dto.OnboardRequestDto;
import com.dodo.dodoserver.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dodo.dodoserver.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dodo.dodoserver.domain.user.dto.ProfileUpdateRequestDto;
import org.springframework.web.bind.annotation.*;

/**
 * 유저 정보 조회 및 온보딩 처리를 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인된 유저의 상세 정보를 조회
     */
    @GetMapping("/me")
    public ApiResponseDto<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponseDto.success(userService.getUserProfileById(principal.getId()));
    }

    /**
     * 특정 유저의 상세 정보를 이메일로 조회
     */
    @GetMapping("/profile/detail")
    public ApiResponseDto<UserProfileResponseDto> getUserProfile(@RequestParam String email) {
        return ApiResponseDto.success(userService.getUserProfileByEmail(email));
    }

    /**
     * 닉네임 중복 여부를 확인
     */
    @GetMapping("/check-nickname")
    public ApiResponseDto<Boolean> checkNickname(@RequestParam String nickname) {
        return ApiResponseDto.success(userService.existsByNickname(nickname));
    }

    /**
     * 사용자의 프로필 정보를 수정 (닉네임, 이미지, 자기소개 등 선택적 수정)
     */
    @PatchMapping("/profile")
    public ApiResponseDto<String> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ProfileUpdateRequestDto requestDto) {

        userService.updateProfile(principal.getId(), requestDto);

        return ApiResponseDto.success("프로필 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * 구글 로그인 후 상세 정보(프로필, 기기 등)를 등록하고 회원가입을 완료
     */
    @PostMapping("/profile")
    public ApiResponseDto<String> postProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid OnboardRequestDto requestDto) {
        
        userService.onboard(principal.getId(), requestDto);
        
        return ApiResponseDto.success("온보딩이 완료되었습니다.");
    }
}

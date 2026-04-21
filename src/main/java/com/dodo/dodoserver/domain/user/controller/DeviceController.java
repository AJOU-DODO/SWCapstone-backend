package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저의 기기 및 FCM 토큰 등록/관리를 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final UserDeviceService userDeviceService;

    /**
     * 현재 로그인한 사용자의 새로운 기기를 등록하거나 기존 기기의 토큰 정보를 업데이트
     */
    @PostMapping
    public ApiResponseDto<String> registerDevice(
            Authentication authentication,
            @RequestBody @Valid DeviceRequestDto requestDto) {
        
        String email = authentication.getName();
        userDeviceService.registerOrUpdateDevice(email, requestDto);
        
        return ApiResponseDto.success("기기가 성공적으로 등록되었습니다.");
    }
}

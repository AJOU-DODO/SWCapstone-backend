package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dodo.dodoserver.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 기기(FCM 토큰 등) 등록 및 관리를 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final UserDeviceService userDeviceService;

    /**
     * 새로운 기기(FCM 토큰)를 등록하거나 기존 정보를 갱신
     */
    @PostMapping
    public ApiResponseDto<String> registerDevice(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid DeviceRequestDto requestDto) {

        userDeviceService.registerOrUpdateDevice(principal.getId(), requestDto);
        return ApiResponseDto.success("기기가 성공적으로 등록되었습니다.");
    }
}

package com.dodo.dodoserver.domain.admin.controller;

import com.dodo.dodoserver.domain.admin.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.admin.dto.UserSanctionRequestDto;
import com.dodo.dodoserver.domain.admin.service.AdminService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 유저 관리 목록 조회
     */
    @GetMapping("/users")
    public ApiResponseDto<Page<UserAdminResponseDto>> getAllUsers(Pageable pageable) {
        return ApiResponseDto.success(adminService.getAllUsers(pageable));
    }

    /**
     * 유저 제재 처리
     */
    @PostMapping("/users/{userId}/sanction")
    public ApiResponseDto<Void> sanctionUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserSanctionRequestDto requestDto) {
        adminService.sanctionUser(userId, requestDto);
        return ApiResponseDto.success(null);
    }
}

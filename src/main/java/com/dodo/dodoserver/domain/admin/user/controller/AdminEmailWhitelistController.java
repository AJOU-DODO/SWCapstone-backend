package com.dodo.dodoserver.domain.admin.user.controller;

import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistRequestDto;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistResponseDto;
import com.dodo.dodoserver.domain.admin.user.service.AdminEmailWhitelistService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/whitelists")
public class AdminEmailWhitelistController {

    private final AdminEmailWhitelistService adminEmailWhitelistService;

    /**
     * 화이트리스트 목록 조회
     */
    @GetMapping
    public ApiResponseDto<List<AdminEmailWhitelistResponseDto>> getWhitelists() {
        return ApiResponseDto.success(adminEmailWhitelistService.getWhitelists());
    }

    /**
     * 화이트리스트 이메일 추가
     */
    @PostMapping
    public ApiResponseDto<AdminEmailWhitelistResponseDto> addWhitelist(
            @Valid @RequestBody AdminEmailWhitelistRequestDto requestDto) {
        return ApiResponseDto.success(adminEmailWhitelistService.addWhitelist(requestDto));
    }

    /**
     * 화이트리스트 이메일 삭제
     */
    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> removeWhitelist(@PathVariable Long id) {
        adminEmailWhitelistService.removeWhitelist(id);
        return ApiResponseDto.success(null);
    }
}

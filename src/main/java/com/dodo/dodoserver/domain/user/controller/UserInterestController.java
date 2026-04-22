package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.user.service.UserInterestService;
import com.dodo.dodoserver.domain.user.dto.UserInterestRequestDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dodo.dodoserver.global.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유저의 관심 카테고리 설정 및 조회를 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/user-interests")
@RequiredArgsConstructor
public class UserInterestController {

    private final UserInterestService userInterestService;

    /**
     * 현재 로그인한 유저의 관심 카테고리 목록을 조회
     */
    @GetMapping
    public ApiResponseDto<List<CategoryResponseDto>> getMyInterests(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponseDto.success(userInterestService.getMyInterests(principal.getEmail()));
    }

    /**
     * 유저의 관심 카테고리를 설정 (기존 정보는 삭제 후 새로 등록)
     */
    @PostMapping
    public ApiResponseDto<String> updateInterests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid UserInterestRequestDto requestDto) {

        userInterestService.updateInterests(principal.getEmail(), requestDto);
        return ApiResponseDto.success("관심 카테고리가 성공적으로 업데이트되었습니다.");
    }
}

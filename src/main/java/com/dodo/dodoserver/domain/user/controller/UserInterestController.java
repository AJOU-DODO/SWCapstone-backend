package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.user.service.UserInterestService;
import com.dodo.dodoserver.domain.user.dto.UserInterestRequestDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 유저의 관심사(카테고리 매핑) 정보를 관리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/users/interests")
@RequiredArgsConstructor
public class UserInterestController {

    private final UserInterestService userInterestService;

    /**
     * 현재 로그인한 사용자가 선택한 관심 카테고리 목록을 조회
     */
    @GetMapping
    public ApiResponseDto<List<CategoryResponseDto>> getMyInterests(Authentication authentication) {
        String email = authentication.getName();
        return ApiResponseDto.success(userInterestService.getMyInterests(email));
    }

    /**
     * 현재 로그인한 사용자의 관심 카테고리 리스트를 일괄 업데이트
     */
    @PutMapping
    public ApiResponseDto<String> updateInterests(
            Authentication authentication,
            @RequestBody @Valid UserInterestRequestDto requestDto) {
        
        String email = authentication.getName();
        userInterestService.updateInterests(email, requestDto);
        
        return ApiResponseDto.success("관심 카테고리가 성공적으로 업데이트되었습니다.");
    }
}

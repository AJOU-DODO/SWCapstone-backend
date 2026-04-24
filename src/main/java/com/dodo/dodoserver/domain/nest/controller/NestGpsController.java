package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dodo.dodoserver.global.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nests")
@RequiredArgsConstructor
public class NestGpsController {

    private final NestService nestService;



    /**
     * 사용자 현재 위치 검증을 통한 둥지 해금
     */
    @PostMapping("/{id}/unlock")
    public ApiResponseDto<String> unlockNest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody @Valid NestUnlockRequestDto requestDto) {
        
        nestService.unlockNest(principal.getId(), id, requestDto);
        return ApiResponseDto.success("둥지가 성공적으로 해금되었습니다.");
    }



    /**
     * 특정 ID 리스트 둥지 요약 정보 조회 (클러스터링 클릭 시 사용)
     */
    @GetMapping("/summaries")
    public ApiResponseDto<List<NestSummaryResponseDto>> getNestsByIds(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam List<Long> ids,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponseDto.success(nestService.getNestsByIds(principal.getId(), ids, pageable.getSort()));
    }

    /**
     * 현재 위치 기반 반경 내 카테고리별 둥지 리스트 조회
     * 정렬 기준 예시: sort=createdAt,desc / sort=viewCount,desc
     */
    @GetMapping
    public ApiResponseDto<Page<NestSummaryResponseDto>> getNearbyNests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) List<Long> categoryIds,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponseDto.success(nestService.getNearNestsByCategory(principal.getId(), latitude, longitude, radiusMeter, categoryIds, pageable));
    }

    /**
     * 현재 위치 기반 반경 내 모든 둥지 핀 정보 조회
     */
    @GetMapping("/pins")
    public ApiResponseDto<List<NestPinResponseDto>> getNearbyPins(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) List<Long> categoryIds) {

        return ApiResponseDto.success(nestService.getNearbyPins(latitude, longitude, radiusMeter, categoryIds));
    }
}

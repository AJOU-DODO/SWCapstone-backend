package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Nest GPS", description = "위치 기반 둥지 조회 API")
@RestController
@RequestMapping("/api/v1/nests")
@RequiredArgsConstructor
public class NestGpsController {

    private final NestService nestService;

    /**
     * 사용자 현재 위치 검증을 통한 둥지 해금
     */
    @Operation(summary = "사용자 현재 위치 검증을 통한 둥지 해금", description = "사용자의 현재 위치가 둥지의 해금 반경 내에 있는지 확인하고 해금합니다.")
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
    @Operation(summary = "ID 리스트 기반 둥지 요약 정보 조회", description = "여러 개의 둥지 ID를 받아 해당 둥지들의 요약 정보를 반환합니다. 클러스터링 클릭 시 등에 사용됩니다.")
    @GetMapping("/summaries")
    public ApiResponseDto<List<NestSummaryResponseDto>> getNestsByIds(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam List<Long> ids,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponseDto.success(nestService.getNestsByIds(principal.getId(), ids, pageable.getSort()));
    }

    /**
     * 현재 위치 기반 반경 내 카테고리별 둥지 리스트 조회 (광고 제외)
     * 정렬 기준 예시: sort=createdAt,desc / sort=viewCount,desc
     */
    @Operation(summary = "반경 내 일반 둥지 리스트 조회", description = "현재 위치 기준 반경 내에 있는 일반 둥지(광고 제외) 목록을 페이징하여 조회합니다.")
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
     * 현재 위치 기반 반경 내 모든 일반 둥지 핀 정보 조회 (광고 제외)
     */
    @Operation(summary = "반경 내 일반 둥지 핀 조회", description = "현재 위치 기준 반경 내에 있는 일반 둥지(광고 제외)들의 ID와 좌표 정보를 조회합니다.")
    @GetMapping("/pins")
    public ApiResponseDto<List<NestPinResponseDto>> getNearbyPins(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) List<Long> categoryIds) {

        return ApiResponseDto.success(nestService.getNearbyPins(latitude, longitude, radiusMeter, categoryIds));
    }

    /**
     * 현재 위치 기반 반경 내 광고 둥지 핀 정보 조회 (우선순위 적용)
     */
    @Operation(summary = "반경 내 광고 둥지 핀 조회", description = "현재 위치 기준 반경 내에 있는 광고 둥지들의 핀 정보를 우선순위에 따라 조회합니다. 조회 시 노출수(impressions)가 증가합니다.")
    @GetMapping("/ad-pins")
    public ApiResponseDto<List<NestPinResponseDto>> getNearbyAdPins(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) List<Long> categoryIds) {

        return ApiResponseDto.success(nestService.getNearbyAdPins(latitude, longitude, radiusMeter, categoryIds));
    }
}

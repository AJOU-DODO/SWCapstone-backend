package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nests")
@RequiredArgsConstructor
public class NestController {

    private final NestService nestService;

    /**
     * 둥지 상세 정보를 조회합니다.
     */
    @GetMapping("/{id}")
    public ApiResponseDto<NestDetailResponseDto> getNestDetail(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();
        return ApiResponseDto.success(nestService.getNestDetail(email, id));
    }

    /**
     * 사용자의 현재 위치를 검증하여 둥지를 해금합니다.
     */
    @PostMapping("/{id}/unlock")
    public ApiResponseDto<String> unlockNest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid NestUnlockRequestDto requestDto) {
        
        String email = authentication.getName();
        nestService.unlockNest(email, id, requestDto);
        return ApiResponseDto.success("둥지가 성공적으로 해금되었습니다.");
    }

    /**
     * 둥지에 대한 리액션(좋아요/싫어요)을 등록하거나 수정합니다.
     */
    @PostMapping("/{id}/reaction")
    public ApiResponseDto<String> handleReaction(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam ReactionType type) {
        
        String email = authentication.getName();
        nestService.handleReaction(email, id, type);
        return ApiResponseDto.success("리액션이 성공적으로 처리되었습니다.");
    }

    /**
     * 현재 위치 기반 반경 내의 카테고리별 둥지 리스트를 조회합니다.
     * 정렬 기준 예시: sort=createdAt,desc / sort=viewCount,desc
     */
    @GetMapping
    public ApiResponseDto<Page<NestSummaryResponseDto>> getNearbyNests(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponseDto.success(nestService.getNearbyNests(latitude, longitude, radiusMeter, categoryId, pageable));
    }

    /**
     * 현재 위치 기반 반경 내의 모든 둥지 핀 정보를 조회합니다.
     */
    @GetMapping("/pins")
    public ApiResponseDto<List<NestPinResponseDto>> getNearbyPins(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter) {

        return ApiResponseDto.success(nestService.getNearbyPins(latitude, longitude, radiusMeter));
    }

    /**
     * 새로운 둥지(Nest)를 생성합니다.
     */
    @PostMapping
    public ApiResponseDto<NestSummaryResponseDto> createNest(
            Authentication authentication,
            @RequestBody @Valid NestCreateRequestDto requestDto) {
        
        String email = authentication.getName();
        return ApiResponseDto.success(nestService.createNest(email, requestDto));
    }
}

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
     * 새로운 둥지(Nest) 생성
     */
    @PostMapping
    public ApiResponseDto<NestSummaryResponseDto> createNest(
            Authentication authentication,
            @RequestBody @Valid NestCreateRequestDto requestDto) {
        
        String email = authentication.getName();
        return ApiResponseDto.success(nestService.createNest(email, requestDto));
    }

    /**
     * 둥지 정보 수정 (작성자 전용)
     */
    @PatchMapping("/{id}")
    public ApiResponseDto<NestSummaryResponseDto> updateNest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid NestUpdateRequestDto requestDto) {
        
        String email = authentication.getName();
        return ApiResponseDto.success(nestService.updateNest(email, id, requestDto));
    }

    /**
     * 둥지 삭제 (작성자 전용)
     */
    @DeleteMapping("/{id}")
    public ApiResponseDto<String> deleteNest(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        nestService.deleteNest(email, id);
        return ApiResponseDto.success("둥지가 성공적으로 삭제되었습니다.");
    }

    /**
     * 둥지 댓글 작성
     */
    @PostMapping("/{id}/comments")
    public ApiResponseDto<String> createComment(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid CommentCreateRequestDto requestDto) {
        
        String email = authentication.getName();
        nestService.createComment(email, id, requestDto);
        return ApiResponseDto.success("댓글이 성공적으로 작성되었습니다.");
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/comments/{commentId}")
    public ApiResponseDto<String> updateComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequestDto requestDto) {
        
        String email = authentication.getName();
        nestService.updateComment(email, commentId, requestDto);
        return ApiResponseDto.success("댓글이 성공적으로 수정되었습니다.");
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ApiResponseDto<String> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId) {
        
        String email = authentication.getName();
        nestService.deleteComment(email, commentId);
        return ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.");
    }

    /**
     * 둥지 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ApiResponseDto<NestDetailResponseDto> getNestDetail(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();
        return ApiResponseDto.success(nestService.getNestDetail(email, id));
    }

    /**
     * 사용자 현재 위치 검증을 통한 둥지 해금
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
     * 둥지 리액션(좋아요/싫어요) 등록 및 수정
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
     * 특정 ID 리스트 둥지 요약 정보 조회 (클러스터링 클릭 시 사용)
     */
    @GetMapping("/summaries")
    public ApiResponseDto<List<NestSummaryResponseDto>> getNestsByIds(
            Authentication authentication,
            @RequestParam List<Long> ids) {
        
        String email = authentication.getName();
        return ApiResponseDto.success(nestService.getNestsByIds(email, ids));
    }

    /**
     * 현재 위치 기반 반경 내 카테고리별 둥지 리스트 조회
     * 정렬 기준 예시: sort=createdAt,desc / sort=viewCount,desc
     */
    @GetMapping
    public ApiResponseDto<Page<NestSummaryResponseDto>> getNearbyNests(
            Authentication authentication,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = authentication.getName();
        return ApiResponseDto.success(nestService.getNearbyNests(email, latitude, longitude, radiusMeter, categoryId, pageable));
    }

    /**
     * 현재 위치 기반 반경 내 모든 둥지 핀 정보 조회
     */
    @GetMapping("/pins")
    public ApiResponseDto<List<NestPinResponseDto>> getNearbyPins(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusMeter) {

        return ApiResponseDto.success(nestService.getNearbyPins(latitude, longitude, radiusMeter));
    }
}

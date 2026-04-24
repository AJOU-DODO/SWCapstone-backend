package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestDraftService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nests/drafts")
@RequiredArgsConstructor
public class NestDraftController {

    private final NestDraftService nestDraftService;

    /**
     * 임시 저장 생성
     */
    @PostMapping
    public ApiResponseDto<NestDraftResponseDto> createDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NestDraftCreateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.createDraft(userPrincipal.getId(), requestDto));
    }

    /**
     * 내 임시 저장 목록 조회
     */
    @GetMapping
    public ApiResponseDto<List<NestDraftResponseDto>> getMyDrafts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(nestDraftService.getMyDrafts(userPrincipal.getId()));
    }

    /**
     * 임시 저장 상세 조회
     */
    @GetMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> getDraftDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.getDraftDetail(userPrincipal.getId(), id));
    }

    /**
     * 임시 저장 수정
     */
    @PatchMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> updateDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody NestDraftUpdateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.updateDraft(userPrincipal.getId(), id, requestDto));
    }

    /**
     * 임시 저장 삭제
     */
    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> deleteDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        nestDraftService.deleteDraft(userPrincipal.getId(), id);
        return ApiResponseDto.success(null);
    }

    /**
     * 임시 저장 발행
     */
    @PostMapping("/{id}/publish")
    public ApiResponseDto<NestSimpleResponseDto> publishDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.publishDraft(userPrincipal.getId(), id));
    }
}

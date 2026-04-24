package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestDraftService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Nest Draft", description = "둥지 임시 저장 API")
@RestController
@RequestMapping("/api/v1/nests/drafts")
@RequiredArgsConstructor
public class NestDraftController {

    private final NestDraftService nestDraftService;

    @Operation(summary = "임시 저장 생성", description = "현 위치 좌표를 기반으로 임시 저장을 생성합니다.")
    @PostMapping
    public ApiResponseDto<NestDraftResponseDto> createDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NestDraftCreateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.createDraft(userPrincipal.getId(), requestDto));
    }

    @Operation(summary = "내 임시 저장 목록 조회", description = "로그인한 사용자의 임시 저장 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<List<NestDraftResponseDto>> getMyDrafts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(nestDraftService.getMyDrafts(userPrincipal.getId()));
    }

    @Operation(summary = "임시 저장 상세 조회", description = "특정 임시 저장의 상세 내용을 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> getDraftDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.getDraftDetail(userPrincipal.getId(), id));
    }

    @Operation(summary = "임시 저장 수정", description = "임시 저장된 내용을 수정합니다.")
    @PatchMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> updateDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody NestDraftUpdateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.updateDraft(userPrincipal.getId(), id, requestDto));
    }

    @Operation(summary = "임시 저장 삭제", description = "임시 저장된 데이터를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> deleteDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        nestDraftService.deleteDraft(userPrincipal.getId(), id);
        return ApiResponseDto.success(null);
    }

    @Operation(summary = "임시 저장 발행", description = "임시 저장된 데이터를 기반으로 정식 둥지를 발행합니다.")
    @PostMapping("/{id}/publish")
    public ApiResponseDto<NestSummaryResponseDto> publishDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.publishDraft(userPrincipal.getId(), id));
    }
}

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

    @PostMapping
    public ApiResponseDto<NestDraftResponseDto> createDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NestDraftCreateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.createDraft(userPrincipal.getId(), requestDto));
    }

    @GetMapping
    public ApiResponseDto<List<NestDraftResponseDto>> getMyDrafts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(nestDraftService.getMyDrafts(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> getDraftDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.getDraftDetail(userPrincipal.getId(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponseDto<NestDraftResponseDto> updateDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody NestDraftUpdateRequestDto requestDto) {
        return ApiResponseDto.success(nestDraftService.updateDraft(userPrincipal.getId(), id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> deleteDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        nestDraftService.deleteDraft(userPrincipal.getId(), id);
        return ApiResponseDto.success(null);
    }

    @PostMapping("/{id}/publish")
    public ApiResponseDto<NestSummaryResponseDto> publishDraft(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(nestDraftService.publishDraft(userPrincipal.getId(), id));
    }
}

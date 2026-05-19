package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDetailResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin Nest", description = "관리자용 게시물 관리 API")
@RestController
@RequestMapping("/api/v1/admin/nests")
@RequiredArgsConstructor
public class AdminNestController {

    private final AdminNestService adminNestService;

    @Operation(summary = "전체 게시물(Nest) 관리 목록 조회", description = "필터 조건 및 정렬에 따라 전체 게시물 목록을 조회합니다. 삭제된 게시물 포함 여부를 선택할 수 있습니다.")
    @GetMapping
    public ApiResponseDto<Page<AdminNestResponseDto>> getNests(
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "ACTIVE_ONLY") String includeDeleted) {
        return ApiResponseDto.success(adminNestService.getNestsForAdmin(pageable, startDate, endDate, sort, includeDeleted));
    }

    @Operation(summary = "관리자 전용 게시물 상세 조회", description = "위치 검증이나 해금 이력과 관계없이 게시물의 원본 상세 데이터(본문, 이미지 등)를 조회합니다.")
    @GetMapping("/{nestId}")
    public ApiResponseDto<AdminNestDetailResponseDto> getNestDetail(@PathVariable Long nestId) {
        return ApiResponseDto.success(adminNestService.getNestDetailForAdmin(nestId));
    }

    @Operation(summary = "관리자 전용 둥지별 댓글 목록 조회", description = "해당 둥지의 모든 댓글(삭제된 것 포함)을 조회하며, 각 댓글의 대기 신고 수를 포함합니다.")
    @GetMapping("/{nestId}/comments")
    public ApiResponseDto<List<AdminCommentResponseDto>> getNestComments(@PathVariable Long nestId) {
        return ApiResponseDto.success(adminNestService.getNestCommentsForAdmin(nestId));
    }

    @Operation(summary = "관리자 전용 게시물 삭제", description = "게시물을 소프트 삭제 처리하며, 작성자에게 FCM 알림을 발송하고 연관 엽서를 원상복구합니다.")
    @DeleteMapping("/{nestId}")
    public ApiResponseDto<Void> deleteNest(
            @PathVariable Long nestId,
            @RequestBody AdminNestDeleteRequestDto requestDto) {
        adminNestService.deleteNestForAdmin(nestId, requestDto);
        return ApiResponseDto.success(null);
    }
}

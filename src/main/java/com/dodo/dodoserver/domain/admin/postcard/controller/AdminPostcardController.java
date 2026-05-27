package com.dodo.dodoserver.domain.admin.postcard.controller;

import com.dodo.dodoserver.domain.admin.postcard.dto.AdminPostcardDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.postcard.service.AdminPostcardService;
import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Postcard", description = "관리자용 엽서 관리 API")
@RestController
@RequestMapping("/api/v1/admin/postcards")
@RequiredArgsConstructor
public class AdminPostcardController {

    private final AdminPostcardService adminPostcardService;

    @Operation(summary = "신고된 엽서 목록 조회", description = "신고가 접수된 엽서 목록을 집계하여 조회합니다. statuses 파라미터로 PENDING, PROCESSED 필터링이 가능합니다.")
    @GetMapping("/reported")
    public ApiResponseDto<Page<AdminPostcardReportResponseDto>> getReportedPostcards(
            Pageable pageable,
            @RequestParam(required = false) List<ReportStatus> statuses,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminPostcardService.getReportedPostcards(pageable, statuses, sort));
    }

    @Operation(summary = "관리자 전용 엽서 강제 삭제", description = "엽서를 소프트 삭제 처리하고 원작자에게 사유와 함께 알림을 발송합니다.")
    @DeleteMapping("/{postcardId}")
    public ApiResponseDto<Void> deletePostcard(
            @PathVariable Long postcardId,
            @RequestBody(required = false) AdminPostcardDeleteRequestDto requestDto) {
        adminPostcardService.deletePostcardForAdmin(postcardId, requestDto != null ? requestDto : new AdminPostcardDeleteRequestDto());
        return ApiResponseDto.success(null);
    }
}

package com.dodo.dodoserver.domain.admin.report.controller;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.ReportDetailResponseDto;
import com.dodo.dodoserver.domain.admin.report.service.AdminReportService;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Report", description = "관리자용 신고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고된 둥지 목록 조회", description = "신고가 접수된 둥지(게시물) 목록을 집계하여 조회합니다. PENDING 또는 PROCESSED 상태인 데이터만 노출됩니다.")
    @GetMapping("/nests")
    public ApiResponseDto<Page<AdminNestReportResponseDto>> getReportedNests(
            Pageable pageable,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminReportService.getReportedNests(pageable, sort));
    }

    @Operation(summary = "신고된 댓글 목록 조회", description = "신고가 접수된 댓글 목록을 집계하여 조회합니다. PENDING 또는 PROCESSED 상태인 데이터만 노출됩니다.")
    @GetMapping("/comments")
    public ApiResponseDto<Page<AdminCommentReportResponseDto>> getReportedComments(
            Pageable pageable,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminReportService.getReportedComments(pageable, sort));
    }

    @Operation(summary = "신고 상세 및 통계 조회", description = "특정 콘텐츠(둥지/댓글/엽서)의 사유별 대기 신고 통계 및 기타 사유 상세 내용을 조회합니다.")
    @GetMapping("/details")
    public ApiResponseDto<ReportDetailResponseDto> getReportDetails(
            @RequestParam ReportType targetType,
            @RequestParam Long targetId) {
        return ApiResponseDto.success(adminReportService.getReportDetails(targetType, targetId));
    }
}

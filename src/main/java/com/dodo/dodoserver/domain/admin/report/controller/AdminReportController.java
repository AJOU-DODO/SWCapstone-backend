package com.dodo.dodoserver.domain.admin.report.controller;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.ReportDetailResponseDto;
import com.dodo.dodoserver.domain.admin.report.service.AdminReportService;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/nests")
    public ApiResponseDto<Page<AdminNestReportResponseDto>> getReportedNests(
            Pageable pageable,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminReportService.getReportedNests(pageable, sort));
    }

    @GetMapping("/comments")
    public ApiResponseDto<Page<AdminCommentReportResponseDto>> getReportedComments(
            Pageable pageable,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminReportService.getReportedComments(pageable, sort));
    }

    @GetMapping("/details")
    public ApiResponseDto<ReportDetailResponseDto> getReportDetails(
            @RequestParam ReportType targetType,
            @RequestParam Long targetId) {
        return ApiResponseDto.success(adminReportService.getReportDetails(targetType, targetId));
    }
}

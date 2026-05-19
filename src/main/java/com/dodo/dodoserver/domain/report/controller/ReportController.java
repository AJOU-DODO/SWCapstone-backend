package com.dodo.dodoserver.domain.report.controller;

import com.dodo.dodoserver.domain.report.dto.ReportRequestDto;
import com.dodo.dodoserver.domain.report.service.ReportService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "도메인 통합 신고하기", description = "둥지, 댓글, 엽서 등 도메인에 관계없이 신고를 접수합니다. 사유가 OTHER일 경우 상세 내용이 필수입니다.")
    @PostMapping
    public ApiResponseDto<Void> createReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReportRequestDto requestDto) {
        
        reportService.createReport(userPrincipal.getId(), requestDto);
        return ApiResponseDto.success(null);
    }
}

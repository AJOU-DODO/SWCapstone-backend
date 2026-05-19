package com.dodo.dodoserver.domain.report.controller;

import com.dodo.dodoserver.domain.report.dto.ReportRequestDto;
import com.dodo.dodoserver.domain.report.service.ReportService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ApiResponseDto<Void> createReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReportRequestDto requestDto) {
        
        reportService.createReport(userPrincipal.getId(), requestDto);
        return ApiResponseDto.success(null);
    }
}

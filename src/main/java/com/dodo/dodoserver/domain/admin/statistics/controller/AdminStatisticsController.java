package com.dodo.dodoserver.domain.admin.statistics.controller;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.service.AdminStatisticsService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/summary")
    public ApiResponseDto<AdminSummaryResponseDto> getSummaryStats() {
        return ApiResponseDto.success(adminStatisticsService.getSummaryStats());
    }
}

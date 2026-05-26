package com.dodo.dodoserver.domain.admin.statistics.controller;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminTrendResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.service.AdminStatisticsService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/summary")
    public ApiResponseDto<AdminSummaryResponseDto> getSummaryStats() {
        return ApiResponseDto.success(adminStatisticsService.getSummaryStats());
    }

    @GetMapping("/postcards/ratio")
    public ApiResponseDto<AdminPostcardRatioResponseDto> getPostcardRatioStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseDto.success(adminStatisticsService.getPostcardRatioStats(startDate, endDate));
    }

    @GetMapping("/trends")
    public ApiResponseDto<List<AdminTrendResponseDto>> getTrafficTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseDto.success(adminStatisticsService.getTrafficTrends(startDate, endDate));
    }
}

package com.dodo.dodoserver.domain.admin.statistics.controller;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminTrendResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.service.AdminStatisticsService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin Statistics", description = "관리자 대시보드 통계 분석 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @Operation(summary = "어드민 현황판 요약 통계", description = "전체 및 당일 생성된 둥지, 댓글, 엽서의 개수를 반환합니다.")
    @GetMapping("/summary")
    public ApiResponseDto<AdminSummaryResponseDto> getSummaryStats() {
        return ApiResponseDto.success(adminStatisticsService.getSummaryStats());
    }

    @Operation(summary = "엽서 교환 프로세스 효율(비율) 분석", description = "특정 기간 동안 생성된 엽서 대비 교환 완료된 엽서의 비율을 분석합니다.")
    @GetMapping("/postcards/ratio")
    public ApiResponseDto<AdminPostcardRatioResponseDto> getPostcardRatioStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseDto.success(adminStatisticsService.getPostcardRatioStats(startDate, endDate));
    }

    @Operation(summary = "차트 시각화용 트래픽 트렌드", description = "특정 기간 동안의 일별 생성 트렌드 데이터를 배열 형태로 반환합니다. 데이터가 없는 날짜는 0으로 보정되어 반환됩니다.")
    @GetMapping("/trends")
    public ApiResponseDto<List<AdminTrendResponseDto>> getTrafficTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponseDto.success(adminStatisticsService.getTrafficTrends(startDate, endDate));
    }
}

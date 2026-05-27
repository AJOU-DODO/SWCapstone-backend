package com.dodo.dodoserver.domain.admin.statistics.service;

import com.dodo.dodoserver.domain.admin.statistics.dao.AdminStatisticsRepositoryCustom;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminTrendResponseDto;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatisticsService {

    private final AdminStatisticsRepositoryCustom adminStatisticsRepository;
    private static final int MAX_TREND_DAYS = 365;

    public AdminSummaryResponseDto getSummaryStats() {
        return adminStatisticsRepository.getSummaryStats();
    }

    public AdminPostcardRatioResponseDto getPostcardRatioStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        AdminPostcardRatioResponseDto stats = adminStatisticsRepository.getPostcardRatioStats(start, end);

        double ratio = 0.0;
        if (stats.getTotalGenerated() > 0) {
            ratio = (stats.getTotalDelivered() / (double) stats.getTotalGenerated()) * 100;
            ratio = Math.round(ratio * 100) / 100.0; // 소수점 둘째 자리까지 반올림
        }

        stats.setDeliveryRatio(ratio);
        return stats;
    }

    public List<AdminTrendResponseDto> getTrafficTrends(LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(6); // 기본값 최근 7일
        }

        validateDateRange(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 결과 맵 초기화 (날짜 순서 보장을 위해 TreeMap 사용)
        Map<LocalDate, AdminTrendResponseDto> trendMap = new TreeMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            trendMap.put(current, AdminTrendResponseDto.empty(current));
            current = current.plusDays(1);
        }

        // 각 도메인별 데이터 조회 및 바인딩
        bindNestTrends(trendMap, startDateTime, endDateTime);
        bindCommentTrends(trendMap, startDateTime, endDateTime);
        bindPostcardTrends(trendMap, startDateTime, endDateTime);

        return new ArrayList<>(trendMap.values());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_TREND_DAYS) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    private void bindNestTrends(Map<LocalDate, AdminTrendResponseDto> trendMap, LocalDateTime start, LocalDateTime end) {
        List<Tuple> nestTrends = adminStatisticsRepository.getNestTrend(start, end);
        for (Tuple tuple : nestTrends) {
            LocalDate date = LocalDate.parse(tuple.get(0, String.class));
            Long count = tuple.get(1, Long.class);
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setNestCount(count != null ? count : 0L);
            }
        }
    }

    private void bindCommentTrends(Map<LocalDate, AdminTrendResponseDto> trendMap, LocalDateTime start, LocalDateTime end) {
        List<Tuple> commentTrends = adminStatisticsRepository.getCommentTrend(start, end);
        for (Tuple tuple : commentTrends) {
            LocalDate date = LocalDate.parse(tuple.get(0, String.class));
            Long count = tuple.get(1, Long.class);
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setCommentCount(count != null ? count : 0L);
            }
        }
    }

    private void bindPostcardTrends(Map<LocalDate, AdminTrendResponseDto> trendMap, LocalDateTime start, LocalDateTime end) {
        List<Tuple> postcardTrends = adminStatisticsRepository.getPostcardTrend(start, end);
        for (Tuple tuple : postcardTrends) {
            LocalDate date = LocalDate.parse(tuple.get(0, String.class));
            Long count = tuple.get(1, Long.class);
            if (trendMap.containsKey(date)) {
                trendMap.get(date).setPostcardCount(count != null ? count : 0L);
            }
        }
    }
}

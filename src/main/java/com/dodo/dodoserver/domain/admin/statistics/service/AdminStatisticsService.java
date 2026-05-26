package com.dodo.dodoserver.domain.admin.statistics.service;

import com.dodo.dodoserver.domain.admin.statistics.dao.AdminStatisticsRepositoryCustom;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatisticsService {

    private final AdminStatisticsRepositoryCustom adminStatisticsRepository;

    public AdminSummaryResponseDto getSummaryStats() {
        return adminStatisticsRepository.getSummaryStats();
    }

    public AdminPostcardRatioResponseDto getPostcardRatioStats() {
        AdminPostcardRatioResponseDto stats = adminStatisticsRepository.getPostcardRatioStats();

        double ratio = 0.0;
        if (stats.getTotalGenerated() > 0) {
            ratio = (stats.getTotalDelivered() / (double) stats.getTotalGenerated()) * 100;
            ratio = Math.round(ratio * 100) / 100.0; // 소수점 둘째 자리까지 반올림
        }

        stats.setDeliveryRatio(ratio);
        return stats;
    }
}

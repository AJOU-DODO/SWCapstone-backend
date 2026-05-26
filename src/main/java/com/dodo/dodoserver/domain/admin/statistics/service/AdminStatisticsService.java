package com.dodo.dodoserver.domain.admin.statistics.service;

import com.dodo.dodoserver.domain.admin.statistics.dao.AdminStatisticsRepositoryCustom;
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
}

package com.dodo.dodoserver.domain.admin.statistics.dao;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;

public interface AdminStatisticsRepositoryCustom {
    AdminSummaryResponseDto getSummaryStats();
}

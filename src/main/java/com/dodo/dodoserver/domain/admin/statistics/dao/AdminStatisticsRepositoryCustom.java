package com.dodo.dodoserver.domain.admin.statistics.dao;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.querydsl.core.Tuple;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminStatisticsRepositoryCustom {
    AdminSummaryResponseDto getSummaryStats();
    AdminPostcardRatioResponseDto getPostcardRatioStats();
    List<Tuple> getNestTrend(LocalDateTime start, LocalDateTime end);
    List<Tuple> getCommentTrend(LocalDateTime start, LocalDateTime end);
    List<Tuple> getPostcardTrend(LocalDateTime start, LocalDateTime end);
}

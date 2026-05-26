package com.dodo.dodoserver.domain.admin.statistics.service;

import com.dodo.dodoserver.domain.admin.statistics.dao.AdminStatisticsRepositoryCustom;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminTrendResponseDto;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTest {

    @Mock
    private AdminStatisticsRepositoryCustom adminStatisticsRepository;

    @InjectMocks
    private AdminStatisticsService adminStatisticsService;

    @Test
    @DisplayName("엽서 교환 비율 계산 - 정상 케이스")
    void getPostcardRatioStats_Success() {
        // given
        AdminPostcardRatioResponseDto mockDto = AdminPostcardRatioResponseDto.builder()
                .totalGenerated(100L)
                .totalDelivered(25L)
                .build();
        given(adminStatisticsRepository.getPostcardRatioStats(any(), any())).willReturn(mockDto);

        // when
        AdminPostcardRatioResponseDto result = adminStatisticsService.getPostcardRatioStats(null, null);

        // then
        assertThat(result.getDeliveryRatio()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("엽서 교환 비율 계산 - 생성된 엽서가 0개일 때")
    void getPostcardRatioStats_ZeroGenerated() {
        // given
        AdminPostcardRatioResponseDto mockDto = AdminPostcardRatioResponseDto.builder()
                .totalGenerated(0L)
                .totalDelivered(0L)
                .build();
        given(adminStatisticsRepository.getPostcardRatioStats(any(), any())).willReturn(mockDto);

        // when
        AdminPostcardRatioResponseDto result = adminStatisticsService.getPostcardRatioStats(null, null);

        // then
        assertThat(result.getDeliveryRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("트래픽 트렌드 조회 - 데이터가 없는 날짜는 0으로 채워짐")
    void getTrafficTrends_FillEmptyDates() {
        // given
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        given(adminStatisticsRepository.getNestTrend(any(), any())).willReturn(Collections.emptyList());
        given(adminStatisticsRepository.getCommentTrend(any(), any())).willReturn(Collections.emptyList());
        given(adminStatisticsRepository.getPostcardTrend(any(), any())).willReturn(Collections.emptyList());

        // when
        List<AdminTrendResponseDto> result = adminStatisticsService.getTrafficTrends(startDate, endDate);

        // then
        assertThat(result).hasSize(7);
        assertThat(result.get(0).getNestCount()).isEqualTo(0L);
        assertThat(result.get(6).getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("트래픽 트렌드 조회 - 데이터 바인딩 검증")
    void getTrafficTrends_DataBinding() {
        // given
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();
        
        Tuple mockTuple = mock(Tuple.class);
        given(mockTuple.get(0, String.class)).willReturn(todayStr);
        given(mockTuple.get(1, Long.class)).willReturn(10L);
        
        given(adminStatisticsRepository.getNestTrend(any(), any())).willReturn(List.of(mockTuple));
        given(adminStatisticsRepository.getCommentTrend(any(), any())).willReturn(Collections.emptyList());
        given(adminStatisticsRepository.getPostcardTrend(any(), any())).willReturn(Collections.emptyList());

        // when
        List<AdminTrendResponseDto> result = adminStatisticsService.getTrafficTrends(today, today);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNestCount()).isEqualTo(10L);
        assertThat(result.get(0).getDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("날짜 유효성 검증 - 시작일이 종료일보다 이후인 경우 예외 발생")
    void validateDateRange_InvalidOrder() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(1);

        // when & then
        assertThatThrownBy(() -> adminStatisticsService.getTrafficTrends(startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("날짜 유효성 검증 - 조회 기간이 365일을 초과하는 경우 예외 발생")
    void validateDateRange_ExceedMaxRange() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(366);

        // when & then
        assertThatThrownBy(() -> adminStatisticsService.getTrafficTrends(startDate, endDate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }
}

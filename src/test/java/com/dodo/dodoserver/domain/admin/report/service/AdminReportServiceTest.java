package com.dodo.dodoserver.domain.admin.report.service;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.ReportDetailResponseDto;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.entity.Report;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @InjectMocks
    private AdminReportService adminReportService;

    @Mock
    private ReportRepository reportRepository;

    @Test
    @DisplayName("신고된 둥지 목록 조회 성공")
    void getReportedNests_success() {
        // given
        AdminNestReportResponseDto responseDto = AdminNestReportResponseDto.builder()
                .nestId(1L)
                .authorNickname("도도새")
                .build();
        Page<AdminNestReportResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto));
        given(reportRepository.findReportedNests(any(), any())).willReturn(page);

        // when
        Page<AdminNestReportResponseDto> result = adminReportService.getReportedNests(PageRequest.of(0, 10), "LATEST");

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("도도새", result.getContent().get(0).getAuthorNickname());
    }

    @Test
    @DisplayName("신고 상세 정보 조회 성공")
    void getReportDetails_success() {
        // given
        Map<String, Long> stats = Map.of("pendingAbuseCount", 5L);
        given(reportRepository.countPendingReportsByTarget(ReportType.NEST, 1L)).willReturn(stats);

        List<String> contents = List.of("기타 사유 상세");
        given(reportRepository.findOtherReportContents(ReportType.NEST, 1L)).willReturn(contents);

        // when
        ReportDetailResponseDto result = adminReportService.getReportDetails(ReportType.NEST, 1L);

        // then
        assertEquals(5L, result.getStats().get("pendingAbuseCount"));
        assertEquals(1, result.getOtherReportContents().size());
        assertEquals("기타 사유 상세", result.getOtherReportContents().get(0));
    }
}

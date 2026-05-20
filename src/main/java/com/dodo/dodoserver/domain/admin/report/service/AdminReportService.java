package com.dodo.dodoserver.domain.admin.report.service;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.ReportDetailResponseDto;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.entity.Report;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;

    public Page<AdminNestReportResponseDto> getReportedNests(Pageable pageable, String sort) {
        return reportRepository.findReportedNests(pageable, sort);
    }

    public Page<AdminCommentReportResponseDto> getReportedComments(Pageable pageable, String sort) {
        return reportRepository.findReportedComments(pageable, sort);
    }

    public ReportDetailResponseDto getReportDetails(ReportType targetType, Long targetId) {
        // 1. 통계 조회
        Map<String, Long> stats = reportRepository.countPendingReportsByTarget(targetType, targetId);

        // 2. OTHER 사유의 상세 내용 리스트 조회 (전용 쿼리 사용으로 성능 최적화)
        List<String> otherReportContents = reportRepository.findOtherReportContents(targetType, targetId);

        return ReportDetailResponseDto.builder()
                .targetType(targetType)
                .targetId(targetId)
                .stats(stats)
                .otherReportContents(otherReportContents)
                .build();
    }
}

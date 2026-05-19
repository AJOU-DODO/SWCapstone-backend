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

        // 2. OTHER 사유의 상세 내용 리스트 조회
        // (네이티브 쿼리 대신 간단하므로 JpaRepository 기본 메서드 활용을 고려할 수 있으나, 
        // 필터링 조건이 명확하므로 별도 쿼리가 유리할 수 있음. 여기서는 일단 전체 조회 후 필터링)
        List<String> otherReportContents = reportRepository.findAll().stream()
                .filter(r -> r.getReportType() == targetType && 
                             r.getTargetId().equals(targetId) && 
                             r.getReason() == ReportReason.OTHER && 
                             r.getStatus() == ReportStatus.PENDING)
                .map(Report::getContent)
                .collect(Collectors.toList());

        return ReportDetailResponseDto.builder()
                .targetType(targetType)
                .targetId(targetId)
                .stats(stats)
                .otherReportContents(otherReportContents)
                .build();
    }
}

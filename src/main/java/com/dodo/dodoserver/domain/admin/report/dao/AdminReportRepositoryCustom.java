package com.dodo.dodoserver.domain.admin.report.dao;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminReportRepositoryCustom {
    Page<AdminNestReportResponseDto> findReportedNests(Pageable pageable, String sort);
    Page<AdminCommentReportResponseDto> findReportedComments(Pageable pageable, String sort);
    Map<String, Long> countPendingReportsByTarget(ReportType targetType, Long targetId);
}

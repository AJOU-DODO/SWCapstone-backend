package com.dodo.dodoserver.domain.report.dao;

import com.dodo.dodoserver.domain.admin.report.dao.AdminReportRepositoryCustom;
import com.dodo.dodoserver.domain.report.entity.Report;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long>, AdminReportRepositoryCustom {
    boolean existsByReporterIdAndReportTypeAndTargetId(Long reporterId, ReportType reportType, Long targetId);

    @Modifying
    @Query("UPDATE Report r SET r.status = :newStatus WHERE r.reportType = :reportType AND r.targetId = :targetId AND r.status = 'PENDING'")
    int updateStatusByTarget(@Param("reportType") ReportType reportType, @Param("targetId") Long targetId, @Param("newStatus") ReportStatus newStatus);
}

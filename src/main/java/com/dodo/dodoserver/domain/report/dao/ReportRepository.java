package com.dodo.dodoserver.domain.report.dao;

import com.dodo.dodoserver.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}

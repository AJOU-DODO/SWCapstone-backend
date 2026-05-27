package com.dodo.dodoserver.domain.admin.postcard.service;

import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostcardService {

    private final ReportRepository reportRepository;

    /**
     * 신고된 엽서 목록 조회
     */
    public Page<AdminPostcardReportResponseDto> getReportedPostcards(Pageable pageable, String sort) {
        return reportRepository.findReportedPostcards(pageable, sort);
    }
}

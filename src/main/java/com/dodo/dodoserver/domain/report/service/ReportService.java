package com.dodo.dodoserver.domain.report.service;

import com.dodo.dodoserver.domain.nest.dao.NestCommentRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.dto.ReportRequestDto;
import com.dodo.dodoserver.domain.report.entity.Report;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final NestRepository nestRepository;
    private final NestCommentRepository nestCommentRepository;
    private final PostcardRepository postcardRepository;
    private final UserRepository userRepository;

    public void createReport(Long userId, ReportRequestDto requestDto) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 대상 존재 여부 검증
        validateTargetExistence(requestDto);

        // 기타 사유일 경우 상세 내용 필수 체크
        if (requestDto.getReason() == ReportReason.OTHER && 
            (requestDto.getContent() == null || requestDto.getContent().isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 신고 생성 및 저장
        Report report = Report.builder()
                .reporter(reporter)
                .reportType(requestDto.getReportType())
                .targetId(requestDto.getTargetId())
                .reason(requestDto.getReason())
                .content(requestDto.getContent())
                .build();

        reportRepository.save(report);
    }

    private void validateTargetExistence(ReportRequestDto requestDto) {
        switch (requestDto.getReportType()) {
            case NEST -> {
                if (!nestRepository.existsById(requestDto.getTargetId())) {
                    throw new BusinessException(ErrorCode.NEST_NOT_FOUND);
                }
            }
            case COMMENT -> {
                if (!nestCommentRepository.existsById(requestDto.getTargetId())) {
                    throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
                }
            }
            case POSTCARD -> {
                if (!postcardRepository.existsById(requestDto.getTargetId())) {
                    throw new BusinessException(ErrorCode.POSTCARD_NOT_FOUND);
                }
            }
        }
    }
}

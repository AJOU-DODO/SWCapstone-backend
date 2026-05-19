package com.dodo.dodoserver.domain.report.service;

import com.dodo.dodoserver.domain.nest.dao.NestCommentRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.dto.ReportRequestDto;
import com.dodo.dodoserver.domain.report.entity.Report;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private NestRepository nestRepository;

    @Mock
    private NestCommentRepository nestCommentRepository;

    @Mock
    private PostcardRepository postcardRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("신고 생성 성공")
    void createReport_success() {
        // given
        User reporter = User.builder().id(1L).nickname("신고자").build();
        ReportRequestDto requestDto = ReportRequestDto.builder()
                .reportType(ReportType.NEST)
                .targetId(100L)
                .reason(ReportReason.ABUSE)
                .content("욕설")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(reporter));
        given(nestRepository.existsById(100L)).willReturn(true);

        // when
        reportService.createReport(1L, requestDto);

        // then
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    @DisplayName("신고 생성 실패 - 대상이 존재하지 않음")
    void createReport_fail_targetNotFound() {
        // given
        User reporter = User.builder().id(1L).nickname("신고자").build();
        ReportRequestDto requestDto = ReportRequestDto.builder()
                .reportType(ReportType.NEST)
                .targetId(100L)
                .reason(ReportReason.ABUSE)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(reporter));
        given(nestRepository.existsById(100L)).willReturn(false);

        // when & then
        assertThrows(BusinessException.class, () -> reportService.createReport(1L, requestDto));
    }

    @Test
    @DisplayName("신고 생성 실패 - 기타 사유 상세 내용 누락")
    void createReport_fail_missingOtherContent() {
        // given
        User reporter = User.builder().id(1L).nickname("신고자").build();
        ReportRequestDto requestDto = ReportRequestDto.builder()
                .reportType(ReportType.NEST)
                .targetId(100L)
                .reason(ReportReason.OTHER)
                .content("") // 빈 내용
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(reporter));
        given(nestRepository.existsById(100L)).willReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> reportService.createReport(1L, requestDto));
    }
}

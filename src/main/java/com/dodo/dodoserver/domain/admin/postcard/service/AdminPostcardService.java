package com.dodo.dodoserver.domain.admin.postcard.service;

import com.dodo.dodoserver.domain.admin.postcard.dto.AdminPostcardDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dodo.dodoserver.global.common.constants.NotificationConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostcardService {

    private final ReportRepository reportRepository;
    private final PostcardRepository postcardRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmService fcmService;

    /**
     * 신고된 엽서 목록 조회
     */
    public Page<AdminPostcardReportResponseDto> getReportedPostcards(Pageable pageable, List<ReportStatus> statuses, String sort) {
        return reportRepository.findReportedPostcards(pageable, statuses, sort);
    }

    /**
     * 관리자 전용 엽서 삭제 및 알림 발송
     */
    @Transactional
    public void deletePostcardForAdmin(Long postcardId, AdminPostcardDeleteRequestDto requestDto) {
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        // 1. FCM 알림 발송
        List<String> tokens = userDeviceRepository.findByUserId(postcard.getOriginalAuthor().getId()).stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());

        if (!tokens.isEmpty()) {
            String reason = (requestDto == null || requestDto.getReason() == null || requestDto.getReason().isBlank())
                    ? DEFAULT_POSTCARD_DELETE_REASON
                    : requestDto.getReason();

            fcmService.sendNotification(new NotificationEvent(
                    tokens,
                    TITLE_POSTCARD_DELETED,
                    reason,
                    Map.of(KEY_TYPE, TYPE_POSTCARD_DELETED, KEY_POSTCARD_ID, postcardId.toString())
            ));
        }

        // 2. 엽서 소프트 삭제
        postcardRepository.delete(postcard);

        // 3. 연관 신고 자동 처리 완료
        reportRepository.updateStatusByTarget(ReportType.POSTCARD, postcardId, ReportStatus.PROCESSED);
    }
}

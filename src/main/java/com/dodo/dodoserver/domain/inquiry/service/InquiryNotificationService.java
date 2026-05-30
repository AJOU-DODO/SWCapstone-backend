package com.dodo.dodoserver.domain.inquiry.service;

import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dodo.dodoserver.global.common.constants.NotificationConstants.*;

/**
 * 1:1 문의 관련 알림 발행 로직을 전담하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryNotificationService {

    private final UserDeviceRepository userDeviceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 문의 답변 등록 알림 발행
     */
    public void sendInquiryAnswerNotification(Inquiry inquiry) {
        User targetUser = inquiry.getUser();
        List<String> fcmTokens = getFcmTokens(targetUser);

        if (fcmTokens.isEmpty()) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put(KEY_TYPE, TYPE_INQUIRY_ANSWERED);
        data.put(KEY_INQUIRY_ID, inquiry.getId().toString());

        eventPublisher.publishEvent(new NotificationEvent(
                fcmTokens,
                TITLE_INQUIRY_ANSWERED,
                BODY_INQUIRY_ANSWERED,
                data
        ));
        
        log.info("문의 답변 알림 이벤트 발행 완료: TargetUser={}, InquiryId={}", targetUser.getEmail(), inquiry.getId());
    }

    private List<String> getFcmTokens(User targetUser) {
        return userDeviceRepository.findByUserId(targetUser.getId()).stream()
                .map(UserDevice::getFcmToken)
                .filter(Objects::nonNull)
                .toList();
    }
}

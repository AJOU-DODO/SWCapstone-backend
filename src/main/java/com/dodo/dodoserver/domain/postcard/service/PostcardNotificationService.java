package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PostcardNotificationService {

    private final UserDeviceRepository userDeviceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 엽서 교환 알림 발행
     */
    public void sendPostcardExchangedNotification(Postcard targetPostcard, Nest nest) {
        User targetUser = targetPostcard.getOriginalAuthor();

        List<String> fcmTokens = getFcmTokens(targetUser);
        if (fcmTokens.isEmpty()) {
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put(KEY_TYPE, TYPE_POSTCARD_EXCHANGED);
        data.put(KEY_NEST_ID, nest.getId().toString());
        data.put(KEY_POSTCARD_ID, targetPostcard.getId().toString());

        String title = TITLE_POSTCARD_EXCHANGED;
        String body = String.format(BODY_POSTCARD_EXCHANGED, nest.getTitle());

        eventPublisher.publishEvent(new NotificationEvent(fcmTokens, title, body, data));
        log.info("엽서 교환 알림 이벤트 발행 완료: TargetUser={}, Nest={}", targetUser.getEmail(), nest.getTitle());
    }

    private List<String> getFcmTokens(User targetUser) {
        return userDeviceRepository.findByUserId(targetUser.getId()).stream()
                .map(UserDevice::getFcmToken)
                .filter(Objects::nonNull)
                .toList();
    }
}

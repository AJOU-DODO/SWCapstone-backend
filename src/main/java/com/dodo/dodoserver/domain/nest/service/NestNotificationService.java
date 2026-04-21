package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
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
 * 둥지 관련 알림(댓글, 답글 등) 발행 로직을 전담하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NestNotificationService {

    private final UserDeviceRepository userDeviceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 댓글 및 대댓글 알림 발행
     */
    public void sendCommentNotification(User commenter, Nest nest, NestComment parent, NestComment savedComment) {
        User targetUser;
        String type;
        String title;
        String body;

        if (parent == null) {
            targetUser = nest.getCreator();
            type = TYPE_COMMENT;
            title = TITLE_NEW_COMMENT;
            body = String.format(BODY_NEW_COMMENT, commenter.getNickname());
        } else {
            targetUser = parent.getUser();
            type = TYPE_REPLY;
            title = TITLE_NEW_REPLY;
            body = String.format(BODY_NEW_REPLY, commenter.getNickname());
        }

        // 본인 글에 본인이 댓글을 단 경우 알림 미발행
        if (commenter.getId().equals(targetUser.getId())) {
            return;
        }

        // 수신자의 FCM 토큰 목록 조회
        List<String> fcmTokens = userDeviceRepository.findByUserId(targetUser.getId()).stream()
                .map(UserDevice::getFcmToken)
                .filter(Objects::nonNull)
                .toList();

        if (fcmTokens.isEmpty()) {
            return;
        }

        // 알림 데이터 구성
        Map<String, String> data = new HashMap<>();
        data.put(KEY_TYPE, type);
        data.put(KEY_NEST_ID, nest.getId().toString());
        data.put(KEY_COMMENT_ID, savedComment.getId().toString());

        // 이벤트 발행
        eventPublisher.publishEvent(new NotificationEvent(fcmTokens, title, body, data));
        log.info("댓글 알림 이벤트 발행 완료: TargetUser={}, Type={}", targetUser.getEmail(), type);
    }
}

package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReactionType;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostcardNotificationServiceTest {

    @InjectMocks
    private PostcardNotificationService postcardNotificationService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("엽서 교환 알림 발행 성공")
    void sendPostcardExchangedNotification_success() {
        // given
        User author = User.builder().id(1L).email("author@test.com").build();
        Postcard postcard = Postcard.builder().id(10L).originalAuthor(author).build();
        Nest nest = Nest.builder().id(100L).title("테스트 둥지").build();
        
        UserDevice device = UserDevice.builder().fcmToken("token123").build();
        given(userDeviceRepository.findByUserId(author.getId())).willReturn(List.of(device));

        // when
        postcardNotificationService.sendPostcardExchangedNotification(postcard, nest);

        // then
        verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("엽서 교환 알림 발행 건너뜀 - FCM 토큰 없음")
    void sendPostcardExchangedNotification_noTokens() {
        // given
        User author = User.builder().id(1L).build();
        Postcard postcard = Postcard.builder().id(10L).originalAuthor(author).build();
        Nest nest = Nest.builder().id(100L).build();
        
        given(userDeviceRepository.findByUserId(author.getId())).willReturn(List.of());

        // when
        postcardNotificationService.sendPostcardExchangedNotification(postcard, nest);

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("엽서 리액션 알림 발행 성공")
    void sendPostcardReactionNotification_success() {
        // given
        User author = User.builder().id(1L).email("author@test.com").build();
        User reactor = User.builder().id(2L).nickname("반응자").build();
        Postcard postcard = Postcard.builder().id(10L).originalAuthor(author).build();
        
        UserDevice device = UserDevice.builder().fcmToken("token123").build();
        given(userDeviceRepository.findByUserId(author.getId())).willReturn(List.of(device));

        // when
        postcardNotificationService.sendPostcardReactionNotification(reactor, postcard, PostcardReactionType.BEST);

        // then
        verify(eventPublisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("엽서 리액션 알림 발행 건너뜀 - 본인 리액션")
    void sendPostcardReactionNotification_ownReaction() {
        // given
        User author = User.builder().id(1L).build();
        Postcard postcard = Postcard.builder().id(10L).originalAuthor(author).build();

        // when
        postcardNotificationService.sendPostcardReactionNotification(author, postcard, PostcardReactionType.BEST);

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }
}

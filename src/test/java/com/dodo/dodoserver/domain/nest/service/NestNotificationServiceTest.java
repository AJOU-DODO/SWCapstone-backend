package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.global.common.constants.NotificationConstants;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NestNotificationServiceTest {

    @InjectMocks
    private NestNotificationService nestNotificationService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User commenter;
    private Nest nest;

    @BeforeEach
    void setUp() {
        commenter = User.builder().id(1L).nickname("작성자").build();
        User nestCreator = User.builder().id(2L).email("creator@test.com").build();
        nest = Nest.builder().id(10L).creator(nestCreator).build();
    }

    @Test
    @DisplayName("일반 댓글 작성 시 둥지 제작자에게 알림 발행")
    void sendCommentNotification_Comment() {
        // given
        NestComment savedComment = NestComment.builder().id(100L).nest(nest).user(commenter).build();
        UserDevice device = UserDevice.builder().fcmToken("token-123").build();

        given(userDeviceRepository.findByUserId(nest.getCreator().getId())).willReturn(List.of(device));

        // when
        nestNotificationService.sendCommentNotification(commenter, nest, null, savedComment);

        // then
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        
        NotificationEvent event = captor.getValue();
        assertThat(event.title()).isEqualTo(NotificationConstants.TITLE_NEW_COMMENT);
        assertThat(event.data().get(NotificationConstants.KEY_TYPE)).isEqualTo(NotificationConstants.TYPE_COMMENT);
    }

    @Test
    @DisplayName("답글 작성 시 부모 댓글 작성자에게 알림 발행")
    void sendCommentNotification_Reply() {
        // given
        User parentAuthor = User.builder().id(3L).email("parent@test.com").build();
        NestComment parent = NestComment.builder().id(50L).user(parentAuthor).build();
        NestComment savedComment = NestComment.builder().id(101L).nest(nest).user(commenter).parent(parent).build();
        UserDevice device = UserDevice.builder().fcmToken("token-456").build();

        given(userDeviceRepository.findByUserId(parentAuthor.getId())).willReturn(List.of(device));

        // when
        nestNotificationService.sendCommentNotification(commenter, nest, parent, savedComment);

        // then
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        NotificationEvent event = captor.getValue();
        assertThat(event.title()).isEqualTo(NotificationConstants.TITLE_NEW_REPLY);
        assertThat(event.data().get(NotificationConstants.KEY_TYPE)).isEqualTo(NotificationConstants.TYPE_REPLY);
    }

    @Test
    @DisplayName("본인 글에 본인이 댓글을 달면 알림이 발행되지 않음")
    void sendCommentNotification_SelfAction() {
        // given
        User sameUser = User.builder().id(2L).build(); // 둥지 제작자와 동일 ID
        Nest selfNest = Nest.builder().id(10L).creator(sameUser).build();
        NestComment savedComment = NestComment.builder().id(100L).nest(selfNest).user(sameUser).build();

        // when
        nestNotificationService.sendCommentNotification(sameUser, selfNest, null, savedComment);

        // then
        verify(eventPublisher, never()).publishEvent(any());
        verify(userDeviceRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("FCM 토큰이 없으면 이벤트가 발행되지 않음")
    void sendCommentNotification_NoTokens() {
        // given
        NestComment savedComment = NestComment.builder().id(100L).nest(nest).user(commenter).build();
        given(userDeviceRepository.findByUserId(nest.getCreator().getId())).willReturn(List.of());

        // when
        nestNotificationService.sendCommentNotification(commenter, nest, null, savedComment);

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("둥지 좋아요 시 둥지 제작자에게 알림 발행")
    void sendNestLikeNotification_success() {
        // given
        User reactor = User.builder().id(1L).nickname("좋아요누른사람").build();
        UserDevice device = UserDevice.builder().fcmToken("token-like").build();
        given(userDeviceRepository.findByUserId(nest.getCreator().getId())).willReturn(List.of(device));

        // when
        nestNotificationService.sendNestLikeNotification(reactor, nest);

        // then
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        NotificationEvent event = captor.getValue();
        assertThat(event.title()).isEqualTo(NotificationConstants.TITLE_NEST_LIKE);
        assertThat(event.data().get(NotificationConstants.KEY_TYPE)).isEqualTo(NotificationConstants.TYPE_NEST_LIKE);
        assertThat(event.data().get(NotificationConstants.KEY_NEST_ID)).isEqualTo(nest.getId().toString());
    }

    @Test
    @DisplayName("댓글 좋아요 시 댓글 작성자에게 알림 발행")
    void sendCommentLikeNotification_success() {
        // given
        User reactor = User.builder().id(1L).nickname("좋아요누른사람").build();
        User commentAuthor = User.builder().id(3L).nickname("댓글작성자").build();
        NestComment comment = NestComment.builder().id(50L).user(commentAuthor).nest(nest).build();
        UserDevice device = UserDevice.builder().fcmToken("token-comment-like").build();
        given(userDeviceRepository.findByUserId(commentAuthor.getId())).willReturn(List.of(device));

        // when
        nestNotificationService.sendCommentLikeNotification(reactor, comment);

        // then
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        NotificationEvent event = captor.getValue();
        assertThat(event.title()).isEqualTo(NotificationConstants.TITLE_COMMENT_LIKE);
        assertThat(event.data().get(NotificationConstants.KEY_TYPE)).isEqualTo(NotificationConstants.TYPE_COMMENT_LIKE);
        assertThat(event.data().get(NotificationConstants.KEY_COMMENT_ID)).isEqualTo(comment.getId().toString());
    }

    @Test
    @DisplayName("본인 글에 본인이 좋아요를 누르면 알림이 발행되지 않음")
    void sendNestLikeNotification_SelfAction() {
        // given
        User nestCreator = nest.getCreator();

        // when
        nestNotificationService.sendNestLikeNotification(nestCreator, nest);

        // then
        verify(eventPublisher, never()).publishEvent(any());
    }
}

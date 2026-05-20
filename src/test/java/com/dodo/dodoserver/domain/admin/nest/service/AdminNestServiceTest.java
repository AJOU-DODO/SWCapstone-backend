package com.dodo.dodoserver.domain.admin.nest.service;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDeleteRequestDto;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminNestServiceTest {

    @InjectMocks
    private AdminNestService adminNestService;

    @Mock
    private NestRepository nestRepository;

    @Mock
    private NestCommentRepository nestCommentRepository;

    @Mock
    private NestCategoryRepository nestCategoryRepository;

    @Mock
    private NestReactionRepository nestReactionRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private PostcardRepository postcardRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private FcmService fcmService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Test
    @DisplayName("둥지 삭제 성공 - FCM 알림 및 소셜 데이터 정리 포함")
    void deleteNest_ForAdmin_success() {
        // given
        User creator = User.builder().id(1L).build();
        Nest nest = Nest.builder().id(100L).creator(creator).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminNestDeleteRequestDto requestDto = new AdminNestDeleteRequestDto();
        // Reflection을 통해 사유 설정 또는 JSON 역직렬화 흉내

        given(nestRepository.findById(100L)).willReturn(Optional.of(nest));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));
        given(nestCommentRepository.findAllByNestId(100L)).willReturn(Collections.emptyList());

        // when
        adminNestService.deleteNestForAdmin(100L, requestDto);

        // then
        verify(fcmService, times(1)).sendNotification(any(NotificationEvent.class));
        verify(postcardRepository, times(1)).recoverSharedPostcardsByNest(nest);
        verify(nestReactionRepository, times(1)).deleteByNest(nest);
        verify(nestCommentRepository, times(1)).deleteAllByNest(nest);
        verify(nestRepository, times(1)).delete(nest);
    }

    @Test
    @DisplayName("둥지 댓글 조회 실패 - 둥지가 존재하지 않거나 삭제된 경우")
    void getNestComments_fail_nestNotFound() {
        // given
        given(nestRepository.existsById(100L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> adminNestService.getNestCommentsForAdmin(100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NEST_NOT_FOUND);
    }

    @Test
    @DisplayName("둥지 댓글 조회 성공 - 빈 목록 반환")
    void getNestComments_success_emptyList() {
        // given
        given(nestRepository.existsById(100L)).willReturn(true);
        given(nestCommentRepository.findAllByNestId(100L)).willReturn(Collections.emptyList());

        // when
        var result = adminNestService.getNestCommentsForAdmin(100L);

        // then
        assertThat(result).isEmpty();
        verify(nestRepository, times(1)).existsById(100L);
        verify(nestCommentRepository, times(1)).findAllByNestId(100L);
    }
}

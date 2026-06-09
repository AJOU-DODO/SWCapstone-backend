package com.dodo.dodoserver.domain.admin.nest.service;

import com.dodo.dodoserver.domain.admin.nest.dto.*;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.entity.*;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.dao.UserProfileRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.domain.user.entity.UserProfile;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dodo.dodoserver.global.common.constants.NotificationConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private FcmService fcmService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Test
    @DisplayName("관리자 둥지 목록 조회 성공")
    void getNestsForAdmin_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 8);
        Page<AdminNestResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        given(nestRepository.findNestsForAdmin(pageable, start, end, "latest", "Y"))
                .willReturn(expectedPage);

        // when
        Page<AdminNestResponseDto> result = adminNestService.getNestsForAdmin(pageable, start, end, "latest", "Y");

        // then
        assertThat(result).isNotNull();
        verify(nestRepository).findNestsForAdmin(pageable, start, end, "latest", "Y");
    }

    @Test
    @DisplayName("관리자 둥지 상세 조회 성공")
    @SuppressWarnings("unchecked")
    void getNestDetailForAdmin_success() {
        // given
        Long nestId = 1L;
        User creator = User.builder().id(10L).nickname("테스터").build();
        Nest nest = Nest.builder()
                .id(nestId)
                .title("제목")
                .content("내용")
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .images(Collections.emptyList())
                .build();

        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(nestCategoryRepository.findAllByNest(nest)).willReturn(Collections.emptyList());
        
        // Querydsl Mocking
        JPAQuery mockQuery = mock(JPAQuery.class);
        given(queryFactory.select(any(Expression.class), any(Expression.class))).willReturn(mockQuery);
        given(mockQuery.from(any(EntityPath.class))).willReturn(mockQuery);
        given(mockQuery.where(any(Predicate[].class))).willReturn(mockQuery);
        given(mockQuery.fetchOne()).willReturn(null);

        given(nestReactionRepository.countByNestAndReactionType(nest, ReactionType.LIKE)).willReturn(5L);
        given(nestReactionRepository.countByNestAndReactionType(nest, ReactionType.DISLIKE)).willReturn(1L);
        given(userProfileRepository.findByUser(creator)).willReturn(Optional.of(UserProfile.builder().profileImageUrl("img").build()));

        // when
        AdminNestDetailResponseDto result = adminNestService.getNestDetailForAdmin(nestId);

        // then
        assertThat(result.getNestId()).isEqualTo(nestId);
        assertThat(result.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("둥지 상세 조회 실패 - 존재하지 않음")
    void getNestDetailForAdmin_fail_notFound() {
        // given
        given(nestRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminNestService.getNestDetailForAdmin(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("둥지 댓글 조회 성공 - 트리 구조 확인")
    @SuppressWarnings("unchecked")
    void getNestCommentsForAdmin_success_treeStructure() {
        // given
        Long nestId = 1L;
        User user = User.builder().id(10L).nickname("유저").build();
        Nest nest = Nest.builder().id(nestId).build();
        
        NestComment parent = NestComment.builder().id(100L).user(user).nest(nest).content("부모").createdAt(LocalDateTime.now().minusMinutes(1)).build();
        NestComment child = NestComment.builder().id(101L).user(user).nest(nest).parent(parent).content("자식").createdAt(LocalDateTime.now()).build();
        
        given(nestRepository.existsById(nestId)).willReturn(true);
        given(nestCommentRepository.findAllByNestId(nestId)).willReturn(List.of(parent, child));
        given(userRepository.findAllById(any())).willReturn(List.of(user));
        given(userProfileRepository.findAllByUserIn(any())).willReturn(Collections.emptyList());

        // Querydsl Mocking for reports and likes
        JPAQuery mockQuery = mock(JPAQuery.class);
        given(queryFactory.select(any(Expression.class), any(Expression.class))).willReturn(mockQuery);
        given(mockQuery.from(any(EntityPath.class))).willReturn(mockQuery);
        lenient().when(mockQuery.where(any(Predicate.class))).thenReturn(mockQuery);
        lenient().when(mockQuery.where(any(Predicate[].class))).thenReturn(mockQuery);
        given(mockQuery.groupBy(any(Expression.class))).willReturn(mockQuery);
        given(mockQuery.fetch()).willReturn(Collections.emptyList());

        // when
        List<AdminCommentResponseDto> result = adminNestService.getNestCommentsForAdmin(nestId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommentId()).isEqualTo(100L);
        assertThat(result.get(0).getChildren()).hasSize(1);
    }

    @Test
    @DisplayName("둥지 삭제 성공 - FCM 알림 및 소셜 데이터 정리 포함")
    void deleteNest_ForAdmin_success() {
        // given
        User creator = User.builder().id(1L).build();
        Nest nest = Nest.builder().id(100L).creator(creator).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminNestDeleteRequestDto requestDto = AdminNestDeleteRequestDto.builder()
                .reason("사유")
                .build();

        given(nestRepository.findById(100L)).willReturn(Optional.of(nest));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));
        given(nestCommentRepository.findAllByNestId(100L)).willReturn(Collections.emptyList());

        // when
        adminNestService.deleteNestForAdmin(100L, requestDto);

        // then
        verify(fcmService, times(1)).sendNotification(argThat(event -> 
                event.body().equals("사유")));
        verify(postcardRepository, times(1)).recoverSharedPostcardsByNest(nest);
        verify(nestReactionRepository, times(1)).deleteByNest(nest);
        verify(nestCommentRepository, times(1)).deleteAllByNest(nest);
        verify(nestRepository, times(1)).delete(nest);
        verify(reportRepository, times(1)).updateStatusByTarget(ReportType.NEST, 100L, ReportStatus.PROCESSED);
    }

    @Test
    @DisplayName("둥지 삭제 시 사유가 없으면 기본 사유로 알림 전송")
    void deleteNest_useDefaultReason_whenReasonIsMissing() {
        // given
        User creator = User.builder().id(1L).build();
        Nest nest = Nest.builder().id(100L).creator(creator).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminNestDeleteRequestDto requestDto = new AdminNestDeleteRequestDto(); // reason is null

        given(nestRepository.findById(100L)).willReturn(Optional.of(nest));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));
        given(nestCommentRepository.findAllByNestId(100L)).willReturn(Collections.emptyList());

        // when
        adminNestService.deleteNestForAdmin(100L, requestDto);

        // then
        verify(fcmService).sendNotification(argThat(event -> 
            event.body().equals(DEFAULT_NEST_DELETE_REASON)
        ));
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

    @Test
    @DisplayName("관리자 전용 댓글 삭제 성공 - FCM 알림 발송 포함")
    void deleteCommentForAdmin_success() {
        // given
        User author = User.builder().id(1L).build();
        Nest nest = Nest.builder().id(100L).build();
        NestComment comment = NestComment.builder().id(10L).user(author).nest(nest).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminCommentDeleteRequestDto requestDto = mock(AdminCommentDeleteRequestDto.class);
        given(requestDto.getReason()).willReturn("부적절한 댓글");

        given(nestCommentRepository.findById(10L)).willReturn(Optional.of(comment));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));

        // when
        adminNestService.deleteCommentForAdmin(10L, requestDto);

        // then
        verify(fcmService, times(1)).sendNotification(argThat(event -> 
                event.body().equals("부적절한 댓글") &&
                event.data().get(KEY_NEST_ID).equals("100") &&
                !event.data().containsKey("commentId")));
        verify(commentLikeRepository, times(1)).deleteByComment(comment);
        verify(nestCommentRepository, times(1)).delete(comment);
        verify(reportRepository, times(1)).updateStatusByTarget(ReportType.COMMENT, 10L, ReportStatus.PROCESSED);
    }

    @Test
    @DisplayName("댓글 삭제 시 사유가 없으면 기본 사유로 알림 전송")
    void deleteComment_useDefaultReason_whenReasonIsMissing() {
        // given
        User author = User.builder().id(1L).build();
        Nest nest = Nest.builder().id(100L).build();
        NestComment comment = NestComment.builder().id(10L).user(author).nest(nest).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminCommentDeleteRequestDto requestDto = new AdminCommentDeleteRequestDto();

        given(nestCommentRepository.findById(10L)).willReturn(Optional.of(comment));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));

        // when
        adminNestService.deleteCommentForAdmin(10L, requestDto);

        // then
        verify(fcmService).sendNotification(argThat(event -> 
            event.body().equals(DEFAULT_COMMENT_DELETE_REASON) &&
            event.data().get(KEY_NEST_ID).equals("100")
        ));
    }
}

package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.*;
import com.dodo.dodoserver.domain.user.dao.UserProfileRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NestServiceTest {

    @InjectMocks
    private NestService nestService;

    @Mock
    private NestRepository nestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private NestCategoryRepository nestCategoryRepository;
    @Mock
    private UnlockHistoryRepository unlockHistoryRepository;
    @Mock
    private NestReactionRepository nestReactionRepository;
    @Mock
    private NestCommentRepository nestCommentRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private NestImageRepository nestImageRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private NestNotificationService nestNotificationService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private User user;
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email(email)
                .nickname("테스터")
                .build();
    }

    // --- 둥지 (Nest) CRUD 테스트 ---

    @Test
    @DisplayName("둥지 생성 성공")
    void createNest_success() {
        NestCreateRequestDto requestDto = new NestCreateRequestDto("제목", "내용", 37.5, 127.0, 100, null, null, false);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.save(any(Nest.class))).willAnswer(inv -> (Nest) inv.getArgument(0));

        NestSummaryResponseDto response = nestService.createNest(user.getId(), requestDto);

        assertThat(response.getContent()).isEqualTo("내용");
        verify(nestRepository).save(any(Nest.class));
    }

    @Test
    @DisplayName("둥지 수정 성공 - 작성자 권한")
    void updateNest_success() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).title("기존제목").images(new ArrayList<>()).build();
        NestUpdateRequestDto requestDto = new NestUpdateRequestDto("수정제목", "수정내용", 200, null, null);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        NestSummaryResponseDto response = nestService.updateNest(user.getId(), nestId, requestDto);

        assertThat(response.getContent()).isEqualTo("수정내용");
        assertThat(nest.getContent()).isEqualTo("수정내용");
    }

    @Test
    @DisplayName("둥지 수정 실패 - 작성자 아님")
    void updateNest_fail_notCreator() {
        Long nestId = 1L;
        User other = User.builder().id(2L).email("other@example.com").build();
        Nest nest = Nest.builder().id(nestId).creator(other).build();
        NestUpdateRequestDto requestDto = new NestUpdateRequestDto("수정제목", null, null, null, null);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        assertThatThrownBy(() -> nestService.updateNest(user.getId(), nestId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_NEST_CREATOR.getMessage());
    }

    @Test
    @DisplayName("둥지 삭제 성공")
    void deleteNest_success() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        nestService.deleteNest(user.getId(), nestId);

        verify(nestRepository).delete(nest);
    }

    @Test
    @DisplayName("둥지 삭제 실패 - 작성자 아님")
    void deleteNest_fail_notCreator() {
        Long nestId = 1L;
        User other = User.builder().id(2L).build();
        Nest nest = Nest.builder().id(nestId).creator(other).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        assertThatThrownBy(() -> nestService.deleteNest(user.getId(), nestId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_NEST_CREATOR.getMessage());
    }

    // --- 해금 (Unlock) & 상세 조회 테스트 ---

    @Test
    @DisplayName("둥지 해금 성공")
    void unlockNest_success() {
        Long nestId = 1L;
        User creator = User.builder().id(2L).build();
        Nest nest = Nest.builder().id(nestId).creator(creator).unlockRadius(100).build();
        NestUnlockRequestDto requestDto = new NestUnlockRequestDto(37.5, 127.0);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(false);
        given(nestRepository.calculateDistance(eq(nestId), any(Point.class))).willReturn(50.0);

        nestService.unlockNest(user.getId(), nestId, requestDto);

        verify(unlockHistoryRepository).save(any(UnlockHistory.class));
    }

    @Test
    @DisplayName("둥지 해금 실패 - 이미 해금됨")
    void unlockNest_fail_alreadyUnlocked() {
        Long nestId = 1L;
        User creator = User.builder().id(2L).build();
        Nest nest = Nest.builder().id(nestId).creator(creator).build();
        NestUnlockRequestDto requestDto = new NestUnlockRequestDto(37.5, 127.0);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(true);

        assertThatThrownBy(() -> nestService.unlockNest(user.getId(), nestId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_UNLOCKED.getMessage());
    }

    @Test
    @DisplayName("둥지 해금 실패 - 작성자 본인")
    void unlockNest_fail_isCreator() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();
        NestUnlockRequestDto requestDto = new NestUnlockRequestDto(37.5, 127.0);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        assertThatThrownBy(() -> nestService.unlockNest(user.getId(), nestId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_UNLOCKED.getMessage());
    }

    @Test
    @DisplayName("둥지 상세 조회 - 미해금 시 ")
    void getNestDetail_locked() {
        Long nestId = 1L;
        User creator = User.builder().id(2L).nickname("작성자").build();
        Nest nest = Nest.builder().id(nestId).title("비밀").content("내용").creator(creator).images(new ArrayList<>()).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(false);
        given(userProfileRepository.findByUser(creator)).willReturn(Optional.empty());
        given(nestCategoryRepository.findAllByNest(nest)).willReturn(new ArrayList<>());

        NestDetailResponseDto response = nestService.getNestDetail(user.getId(), nestId);

        assertThat(response.getContent()).isEqualTo("내용");
        assertThat(response.isUnlocked()).isFalse();
    }

    @Test
    @DisplayName("둥지 댓글 리스트 조회 성공")
    void getCommentsByNestId_success() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).build();
        NestComment comment = NestComment.builder().id(10L).user(user).content("댓글").children(new ArrayList<>()).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(nestCommentRepository.findAllByNestWithUser(nest)).willReturn(List.of(comment));
        given(userProfileRepository.findAllByUserIn(any())).willReturn(new ArrayList<>());
        given(commentLikeRepository.findAllByUserAndCommentIn(any(), any())).willReturn(new ArrayList<>());

        List<CommentResponseDto> result = nestService.getCommentsByNestId(user.getId(), nestId, "DEFAULT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
    }

    @Test
    @DisplayName("댓글 좋아요 등록 성공")
    void handleCommentLike_create() {
        Long commentId = 10L;
        NestComment comment = NestComment.builder().id(commentId).user(user).likeCount(0L).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestCommentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentLikeRepository.findByUserAndComment(user, comment)).willReturn(Optional.empty());

        nestService.handleCommentLike(user.getId(), commentId);

        assertThat(comment.getLikeCount()).isEqualTo(1L);
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공")
    void handleCommentLike_cancel() {
        Long commentId = 10L;
        NestComment comment = NestComment.builder().id(commentId).user(user).likeCount(1L).build();
        CommentLike like = CommentLike.builder().user(user).comment(comment).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestCommentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentLikeRepository.findByUserAndComment(user, comment)).willReturn(Optional.of(like));

        nestService.handleCommentLike(user.getId(), commentId);

        assertThat(comment.getLikeCount()).isEqualTo(0L);
        verify(commentLikeRepository).delete(like);
    }

    // --- 댓글 (Comment) 테스트 ---

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_success() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("댓글내용", null);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));

        nestService.createComment(user.getId(), nestId, requestDto);

        verify(nestCommentRepository).save(any(NestComment.class));
            verify(nestNotificationService).sendCommentNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 부모 댓글이 다른 둥지에 속함")
    void createComment_fail_parentFromOtherNest() {
        Long nestId = 1L;
        Long otherNestId = 2L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();
        Nest otherNest = Nest.builder().id(otherNestId).build();
        
        NestComment parentComment = NestComment.builder().id(10L).nest(otherNest).build();
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("내용", 10L);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(nestCommentRepository.findById(10L)).willReturn(Optional.of(parentComment));

        assertThatThrownBy(() -> nestService.createComment(user.getId(), nestId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        Long commentId = 10L;
        NestComment comment = NestComment.builder().id(commentId).user(user).content("기존내용").build();
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정내용");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        nestService.updateComment(user.getId(), commentId, requestDto);

        assertThat(comment.getContent()).isEqualTo("수정내용");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 아님")
    void updateComment_fail_notAuthor() {
        Long commentId = 10L;
        User other = User.builder().id(2L).build();
        NestComment comment = NestComment.builder().id(commentId).user(other).build();
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정내용");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> nestService.updateComment(user.getId(), commentId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        Long commentId = 10L;
        NestComment comment = NestComment.builder().id(commentId).user(user).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        nestService.deleteComment(user.getId(), commentId);

        verify(nestCommentRepository).delete(comment);
    }

    // --- 리액션 (Reaction) 테스트 ---

    @Test
    @DisplayName("리액션 등록 성공")
    void handleReaction_create() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(nestReactionRepository.findByUserAndNest(user, nest)).willReturn(Optional.empty());

        nestService.handleReaction(user.getId(), nestId, ReactionType.LIKE);

        verify(nestReactionRepository).save(any(NestReaction.class));
    }

    @Test
    @DisplayName("리액션 취소 성공")
    void handleReaction_cancel() {
        Long nestId = 1L;
        Nest nest = Nest.builder().id(nestId).creator(user).build();
        NestReaction reaction = NestReaction.builder().user(user).nest(nest).reactionType(ReactionType.LIKE).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nestId)).willReturn(Optional.of(nest));
        given(nestReactionRepository.findByUserAndNest(user, nest)).willReturn(Optional.of(reaction));

        nestService.handleReaction(user.getId(), nestId, ReactionType.LIKE);

        verify(nestReactionRepository).delete(reaction);
    }

    // --- 조회 (Search) 테스트 ---

    @Test
    @DisplayName("ID 리스트로 요약 조회 성공")
    void getNestsByIds_success() {
        List<Long> ids = List.of(1L, 2L);
        Nest n1 = Nest.builder().id(1L).creator(user).images(new ArrayList<>()).build();
        Nest n2 = Nest.builder().id(2L).creator(user).images(new ArrayList<>()).build();

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findAllById(ids)).willReturn(List.of(n1, n2));
        given(unlockHistoryRepository.findAllByUserAndNestIn(eq(user), any())).willReturn(new ArrayList<>());

        List<NestSummaryResponseDto> result = nestService.getNestsByIds(user.getId(), ids);

        assertThat(result).hasSize(2);
    }
}

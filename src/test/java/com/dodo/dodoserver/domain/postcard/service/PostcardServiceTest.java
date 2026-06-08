package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.domain.postcard.dao.PostcardReactionRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.dto.PostcardCreateRequestDto;
import com.dodo.dodoserver.domain.postcard.dto.PostcardExchangeCheckResponseDto;
import com.dodo.dodoserver.domain.postcard.dto.PostcardExchangeRequestDto;
import com.dodo.dodoserver.domain.postcard.dto.PostcardResponseDto;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReaction;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReactionType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostcardServiceTest {

    @InjectMocks
    private PostcardService postcardService;

    @Mock
    private PostcardRepository postcardRepository;
    @Mock
    private PostcardRedisService postcardRedisService;
    @Mock
    private NestRepository nestRepository;
    @Mock
    private NestService nestService;
    @Mock
    private PostcardNotificationService postcardNotificationService;
    @Mock
    private PostcardReactionRepository postcardReactionRepository;
    @Mock
    private UserRepository userRepository;

    private User user;
    private Nest nest;
    private Postcard myPostcard;
    private Postcard targetPostcard;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).nickname("나").build();
        User author = User.builder().id(2L).nickname("작가").build();
        User nestCreator = User.builder().id(3L).nickname("둥지주인").build();
        nest = Nest.builder().id(1L).title("테스트 둥지").creator(nestCreator).build();
        
        myPostcard = Postcard.builder()
                .id(10L)
                .originalAuthor(user)
                .currentOwner(user)
                .isShared(false)
                .isExchanged(false)
                .build();
                
        targetPostcard = Postcard.builder()
                .id(20L)
                .originalAuthor(author)
                .isShared(true)
                .isExchanged(false)
                .build();
    }

    @Test
    @DisplayName("엽서 생성 성공")
    void createPostcard_success() {
        // given
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("http://image.url", "엽서 내용");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.save(any(Postcard.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PostcardResponseDto response = postcardService.createPostcard(user.getId(), requestDto);

        // then
        assertThat(response.getContent()).isEqualTo("엽서 내용");
        assertThat(response.getImageUrl()).isEqualTo("http://image.url");
        verify(postcardRepository).save(any(Postcard.class));
    }

    @Test
    @DisplayName("엽서 교환 가능 여부 확인 - 가능")
    void checkExchangeAvailability_true() {
        // given
        given(postcardRedisService.getRemainingExchangeCount(user.getId())).willReturn(3);

        // when
        PostcardExchangeCheckResponseDto response = postcardService.checkExchangeAvailability(user.getId());

        // then
        assertThat(response.isCanExchange()).isTrue();
        assertThat(response.getRemainingCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("엽서 교환 가능 여부 확인 - 불가능(횟수 초과)")
    void checkExchangeAvailability_false() {
        // given
        given(postcardRedisService.getRemainingExchangeCount(user.getId())).willReturn(0);

        // when
        PostcardExchangeCheckResponseDto response = postcardService.checkExchangeAvailability(user.getId());

        // then
        assertThat(response.isCanExchange()).isFalse();
        assertThat(response.getRemainingCount()).isEqualTo(0);
        assertThat(response.getReason()).isEqualTo(ErrorCode.EXCHANGE_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 성공")
    void exchangePostcard_success() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));
        given(postcardRepository.findSharedPostcardByNestForUpdate(nest)).willReturn(Optional.of(targetPostcard));

        // when
        PostcardResponseDto response = postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto);

        // then
        assertThat(response.getId()).isEqualTo(targetPostcard.getId());
        assertThat(targetPostcard.getCurrentOwner()).isEqualTo(user);
        assertThat(targetPostcard.isExchanged()).isTrue();
        assertThat(myPostcard.isShared()).isTrue();
        assertThat(myPostcard.getNest()).isEqualTo(nest);

        verify(postcardRedisService).checkAndIncrementExchangeCount(user.getId());
        verify(postcardNotificationService).sendPostcardExchangedNotification(targetPostcard, nest);
    }

    @Test
    @DisplayName("엽서 교환 실패 - 둥지 찾을 수 없음")
    void exchangePostcard_fail_nestNotFound() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.ofNullable(null));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NEST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 둥지 미해금")
    void exchangePostcard_fail_nestNotUnlocked() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NEST_NOT_UNLOCKED.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 내 엽서 찾을 수 없음")
    void exchangePostcard_fail_myPostcardNotFound() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.POSTCARD_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 내 엽서 소유자 아님")
    void exchangePostcard_fail_notOwner() {
        // given
        User someoneElse = User.builder().id(999L).build();
        myPostcard.setCurrentOwner(someoneElse);
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_POSTCARD_OWNER.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 이미 교환된 엽서")
    void exchangePostcard_fail_alreadyExchanged() {
        // given
        myPostcard.setExchanged(true);
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_EXCHANGED.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 둥지에 교환 가능한 엽서 없음")
    void exchangePostcard_fail_noTargetPostcard() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));
        given(postcardRepository.findSharedPostcardByNestForUpdate(nest)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NO_AVAILABLE_POSTCARD_IN_NEST.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 본인 엽서인 경우")
    void exchangePostcard_fail_ownPostcard() {
        // given
        targetPostcard.setOriginalAuthor(user); // 둥지에 있는 엽서가 내가 쓴 것
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(nestService.isNestUnlockedForUser(nest, user)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));
        given(postcardRepository.findSharedPostcardByNestForUpdate(nest)).willReturn(Optional.of(targetPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CANNOT_EXCHANGE_OWN_POSTCARD.getMessage());
    }

    @Test
    @DisplayName("엽서 인벤토리 조회 성공 - 전체 보기")
    void getPostcardInventory_all_success() {
        // given
        List<Postcard> inventoryList = List.of(myPostcard, targetPostcard);
        Page<Postcard> inventoryPage = new PageImpl<>(inventoryList);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findInventoryByUser(eq(user), any(Pageable.class))).willReturn(inventoryPage);
        
        PostcardReaction reaction = PostcardReaction.builder()
                .postcard(targetPostcard)
                .reactionType(PostcardReactionType.TOUCHED)
                .build();
        given(postcardReactionRepository.findAllByPostcardIn(inventoryList)).willReturn(List.of(reaction));

        // when
        Page<PostcardResponseDto> result = postcardService.getPostcardInventory(user.getId(), "ALL", Pageable.unpaged());

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(myPostcard.getId());
        assertThat(result.getContent().get(1).getId()).isEqualTo(targetPostcard.getId());
    }

    @Test
    @DisplayName("엽서 인벤토리 조회 성공 - 필터링 테스트")
    void getPostcardInventory_filters_success() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findCreatedByUser(eq(user), any())).willReturn(Page.empty());
        given(postcardRepository.findCreatedNotSharedByUser(eq(user), any())).willReturn(Page.empty());
        given(postcardRepository.findCreatedSharedByUser(eq(user), any())).willReturn(Page.empty());
        given(postcardRepository.findCreatedExchangedByUser(eq(user), any())).willReturn(Page.empty());
        given(postcardRepository.findAcquiredByUser(eq(user), any())).willReturn(Page.empty());

        // when & then
        postcardService.getPostcardInventory(user.getId(), "CREATED", Pageable.unpaged());
        verify(postcardRepository).findCreatedByUser(eq(user), any());

        postcardService.getPostcardInventory(user.getId(), "CREATED_NOT_SHARED", Pageable.unpaged());
        verify(postcardRepository).findCreatedNotSharedByUser(eq(user), any());

        postcardService.getPostcardInventory(user.getId(), "CREATED_SHARED", Pageable.unpaged());
        verify(postcardRepository).findCreatedSharedByUser(eq(user), any());

        postcardService.getPostcardInventory(user.getId(), "CREATED_EXCHANGED", Pageable.unpaged());
        verify(postcardRepository).findCreatedExchangedByUser(eq(user), any());

        postcardService.getPostcardInventory(user.getId(), "ACQUIRED", Pageable.unpaged());
        verify(postcardRepository).findAcquiredByUser(eq(user), any());
    }

    @Test
    @DisplayName("엽서 인벤토리 조회 - 빈 목록인 경우")
    void getPostcardInventory_empty() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findInventoryByUser(eq(user), any(Pageable.class))).willReturn(Page.empty());

        // when
        Page<PostcardResponseDto> result = postcardService.getPostcardInventory(user.getId(), "ALL", Pageable.unpaged());

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("엽서 상세 조회 성공 - 리액션 없음")
    void getPostcardDetail_noReaction() {
        // given
        given(postcardRepository.findById(targetPostcard.getId())).willReturn(Optional.of(targetPostcard));
        given(postcardReactionRepository.findByPostcard(targetPostcard)).willReturn(Optional.empty());

        // when
        PostcardResponseDto response = postcardService.getPostcardDetail(user.getId(), targetPostcard.getId());

        // then
        assertThat(response.getId()).isEqualTo(targetPostcard.getId());
        assertThat(response.getReactionType()).isNull();
    }

    @Test
    @DisplayName("엽서 수정 성공")
    void updatePostcard_success() {
        // given
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("http://new.url", "새 내용");
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when
        PostcardResponseDto response = postcardService.updatePostcard(user.getId(), myPostcard.getId(), requestDto);

        // then
        assertThat(response.getContent()).isEqualTo("새 내용");
        assertThat(response.getImageUrl()).isEqualTo("http://new.url");
    }

    @Test
    @DisplayName("엽서 수정 실패 - 소유자 아님")
    void updatePostcard_fail_notOwner() {
        // given
        User someoneElse = User.builder().id(999L).build();
        myPostcard.setCurrentOwner(someoneElse);
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("url", "content");
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.updatePostcard(user.getId(), myPostcard.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_POSTCARD_OWNER.getMessage());
    }

    @Test
    @DisplayName("엽서 수정 실패 - 이미 공유/교환됨")
    void updatePostcard_fail_invalidStatus() {
        // given
        myPostcard.setShared(true);
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("url", "content");
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.updatePostcard(user.getId(), myPostcard.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("엽서 삭제 성공")
    void deletePostcard_success() {
        // given
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when
        postcardService.deletePostcard(user.getId(), myPostcard.getId());

        // then
        verify(postcardRepository).delete(myPostcard);
    }

    @Test
    @DisplayName("엽서 삭제 실패 - 이미 공유 중")
    void deletePostcard_fail_alreadyShared() {
        // given
        myPostcard.setShared(true);
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.deletePostcard(user.getId(), myPostcard.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_SHARED.getMessage());
    }

    @Test
    @DisplayName("리액션 추가 성공 - 신규 리액션")
    void addReaction_success_new() {
        // given
        targetPostcard.setCurrentOwner(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findById(targetPostcard.getId())).willReturn(Optional.of(targetPostcard));
        given(postcardReactionRepository.findByPostcardAndUser(targetPostcard, user)).willReturn(Optional.empty());

        // when
        postcardService.addReaction(user.getId(), targetPostcard.getId(), PostcardReactionType.BEST);

        // then
        verify(postcardReactionRepository).save(any(PostcardReaction.class));
        verify(postcardNotificationService).sendPostcardReactionNotification(eq(user), eq(targetPostcard), eq(PostcardReactionType.BEST));
    }

    @Test
    @DisplayName("리액션 추가 성공 - 리액션 취소(동일 타입)")
    void addReaction_success_cancel() {
        // given
        targetPostcard.setCurrentOwner(user);
        PostcardReaction reaction = PostcardReaction.builder()
                .postcard(targetPostcard)
                .user(user)
                .reactionType(PostcardReactionType.BEST)
                .build();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findById(targetPostcard.getId())).willReturn(Optional.of(targetPostcard));
        given(postcardReactionRepository.findByPostcardAndUser(targetPostcard, user)).willReturn(Optional.of(reaction));

        // when
        postcardService.addReaction(user.getId(), targetPostcard.getId(), PostcardReactionType.BEST);

        // then
        verify(postcardReactionRepository).delete(reaction);
    }

    @Test
    @DisplayName("리액션 추가 성공 - 타입 변경")
    void addReaction_success_changeType() {
        // given
        targetPostcard.setCurrentOwner(user);
        PostcardReaction reaction = PostcardReaction.builder()
                .postcard(targetPostcard)
                .user(user)
                .reactionType(PostcardReactionType.BEST)
                .build();
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findById(targetPostcard.getId())).willReturn(Optional.of(targetPostcard));
        given(postcardReactionRepository.findByPostcardAndUser(targetPostcard, user)).willReturn(Optional.of(reaction));

        // when
        postcardService.addReaction(user.getId(), targetPostcard.getId(), PostcardReactionType.TOUCHED);

        // then
        assertThat(reaction.getReactionType()).isEqualTo(PostcardReactionType.TOUCHED);
        verify(postcardNotificationService).sendPostcardReactionNotification(eq(user), eq(targetPostcard), eq(PostcardReactionType.TOUCHED));
    }
}

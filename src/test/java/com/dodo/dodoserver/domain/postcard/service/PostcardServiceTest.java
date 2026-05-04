package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.dao.UnlockHistoryRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.dao.PostcardReactionRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    private UnlockHistoryRepository unlockHistoryRepository;
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
        nest = Nest.builder().id(1L).title("테스트 둥지").build();
        
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
    @DisplayName("엽서 교환 성공")
    void exchangePostcard_success() {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(true);
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
    @DisplayName("엽서 교환 실패 - 본인 엽서인 경우")
    void exchangePostcard_fail_ownPostcard() {
        // given
        targetPostcard.setOriginalAuthor(user); // 둥지에 있는 엽서가 내가 쓴 것
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));
        given(postcardRepository.findSharedPostcardByNestForUpdate(nest)).willReturn(Optional.of(targetPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CANNOT_EXCHANGE_OWN_POSTCARD.getMessage());
    }

    @Test
    @DisplayName("엽서 교환 실패 - 이미 공유 중인 내 엽서 제출")
    void exchangePostcard_fail_alreadyShared() {
        // given
        myPostcard.setShared(true);
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(myPostcard.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(nestRepository.findById(nest.getId())).willReturn(Optional.of(nest));
        given(unlockHistoryRepository.existsByUserAndNest(user, nest)).willReturn(true);
        given(postcardRepository.findByIdForUpdate(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.exchangePostcard(user.getId(), nest.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_SHARED.getMessage());
    }

    @Test
    @DisplayName("엽서 인벤토리 조회 성공")
    void getPostcardInventory_success() {
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
        Page<PostcardResponseDto> result = postcardService.getPostcardInventory(user.getId(), Pageable.unpaged());

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(myPostcard.getId());
        assertThat(result.getContent().get(0).getReactionType()).isNull();
        
        assertThat(result.getContent().get(1).getId()).isEqualTo(targetPostcard.getId());
        assertThat(result.getContent().get(1).getReactionType()).isEqualTo(PostcardReactionType.TOUCHED);
    }

    @Test
    @DisplayName("엽서 상세 조회 시 리액션 정보 포함 확인")
    void getPostcardDetail_withReaction() {
        // given
        given(postcardRepository.findById(targetPostcard.getId())).willReturn(Optional.of(targetPostcard));
        
        PostcardReaction reaction = PostcardReaction.builder()
                .postcard(targetPostcard)
                .reactionType(PostcardReactionType.BEST)
                .build();
        given(postcardReactionRepository.findByPostcard(targetPostcard)).willReturn(Optional.of(reaction));

        // when
        PostcardResponseDto response = postcardService.getPostcardDetail(user.getId(), targetPostcard.getId());

        // then
        assertThat(response.getId()).isEqualTo(targetPostcard.getId());
        assertThat(response.getReactionType()).isEqualTo(PostcardReactionType.BEST);
    }

    @Test
    @DisplayName("리액션 추가 실패 - 원작자 본인인 경우")
    void addReaction_fail_originalAuthor() {
        // given
        myPostcard.setOriginalAuthor(user);
        myPostcard.setCurrentOwner(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postcardRepository.findById(myPostcard.getId())).willReturn(Optional.of(myPostcard));

        // when & then
        assertThatThrownBy(() -> postcardService.addReaction(user.getId(), myPostcard.getId(), PostcardReactionType.TOUCHED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CANNOT_REACTION_OWN_POSTCARD.getMessage());
    }
}

package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.nest.dao.NestDraftRepository;
import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NestDraftServiceTest {

    @InjectMocks
    private NestDraftService nestDraftService;

    @Mock
    private NestDraftRepository nestDraftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NestService nestService;

    @Spy
    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    @DisplayName("임시 저장 생성 성공")
    void createDraft_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        NestDraftCreateRequestDto requestDto = new NestDraftCreateRequestDto(37.5, 127.0, "제목", "내용", 100, List.of(1L), List.of("url"));
        
        Point point = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
        NestDraft draft = NestDraft.builder()
                .id(10L)
                .creator(user)
                .point(point)
                .title("제목")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(nestDraftRepository.save(any())).willReturn(draft);

        // when
        NestDraftResponseDto response = nestDraftService.createDraft(userId, requestDto);

        // then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("제목");
        verify(nestDraftRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("내 임시 저장 목록 조회 성공")
    void getMyDrafts_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        NestDraft draft = NestDraft.builder().id(10L).creator(user).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(nestDraftRepository.findAllByCreatorOrderByCreatedAtDesc(user)).willReturn(List.of(draft));

        // when
        List<NestDraftResponseDto> response = nestDraftService.getMyDrafts(userId);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("임시 저장 상세 조회 실패 - 권한 없음")
    void getDraftDetail_fail_notOwner() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long draftId = 10L;
        User otherUser = User.builder().id(otherUserId).build();
        NestDraft draft = NestDraft.builder().id(draftId).creator(otherUser).build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));

        // when & then
        assertThatThrownBy(() -> nestDraftService.getDraftDetail(userId, draftId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.HANDLE_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("임시 저장 삭제 성공")
    void deleteDraft_success() {
        // given
        Long userId = 1L;
        Long draftId = 10L;
        User user = User.builder().id(userId).build();
        NestDraft draft = NestDraft.builder().id(draftId).creator(user).build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));

        // when
        nestDraftService.deleteDraft(userId, draftId);

        // then
        verify(nestDraftRepository, times(1)).delete(draft);
    }

    @Test
    @DisplayName("임시 저장 발행 성공")
    void publishDraft_success() {
        // given
        Long userId = 1L;
        Long draftId = 10L;
        User user = User.builder().id(userId).build();
        Point point = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
        NestDraft draft = NestDraft.builder()
                .id(draftId)
                .creator(user)
                .point(point)
                .title("발행제목")
                .content("발행내용")
                .unlockRadius(100)
                .categoryIds(List.of(1L))
                .build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));
        given(nestService.createNest(eq(userId), any())).willReturn(NestSummaryResponseDto.builder().id(100L).build());

        // when
        NestSummaryResponseDto response = nestDraftService.publishDraft(userId, draftId);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        verify(nestService, times(1)).createNest(eq(userId), any());
        verify(nestDraftRepository, times(1)).delete(draft);
    }

    @Test
    @DisplayName("임시 저장 발행 실패 - 필수값(제목) 없음")
    void publishDraft_fail_noTitle() {
        // given
        Long userId = 1L;
        Long draftId = 10L;
        User user = User.builder().id(userId).build();
        NestDraft draft = NestDraft.builder()
                .id(draftId)
                .creator(user)
                .title(null)
                .content("내용")
                .unlockRadius(100)
                .categoryIds(List.of(1L))
                .build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));

        // when & then
        assertThatThrownBy(() -> nestDraftService.publishDraft(userId, draftId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DRAFT_NOT_PUBLISHABLE.getMessage());
    }

    @Test
    @DisplayName("임시 저장 발행 실패 - 필수값(내용) 없음")
    void publishDraft_fail_noContent() {
        // given
        Long userId = 1L;
        Long draftId = 10L;
        User user = User.builder().id(userId).build();
        NestDraft draft = NestDraft.builder()
                .id(draftId)
                .creator(user)
                .title("제목")
                .content(null)
                .unlockRadius(100)
                .categoryIds(List.of(1L))
                .build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));

        // when & then
        assertThatThrownBy(() -> nestDraftService.publishDraft(userId, draftId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("임시 저장 발행 실패 - 필수값(카테고리) 없음")
    void publishDraft_fail_noCategories() {
        // given
        Long userId = 1L;
        Long draftId = 10L;
        User user = User.builder().id(userId).build();
        NestDraft draft = NestDraft.builder()
                .id(draftId)
                .creator(user)
                .title("제목")
                .content("내용")
                .unlockRadius(100)
                .categoryIds(null)
                .build();

        given(nestDraftRepository.findById(draftId)).willReturn(Optional.of(draft));

        // when & then
        assertThatThrownBy(() -> nestDraftService.publishDraft(userId, draftId))
                .isInstanceOf(BusinessException.class);
    }
}

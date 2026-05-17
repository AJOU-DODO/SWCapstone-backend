package com.dodo.dodoserver.domain.notice.service;

import com.dodo.dodoserver.domain.notice.dao.NoticeRepository;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import com.dodo.dodoserver.domain.notice.entity.Notice;
import com.dodo.dodoserver.domain.notice.entity.NoticeCategory;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("사용자용 목록 조회 성공")
    void getPublishedNotices_success() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Notice notice = Notice.builder()
                .id(1L)
                .category(NoticeCategory.UPDATE)
                .title("발행된 공지")
                .isPublished(true)
                .build();
        Page<Notice> page = new PageImpl<>(List.of(notice));

        given(noticeRepository.findAllByIsPublishedTrueAndDeletedAtIsNull(pageable)).willReturn(page);

        // when
        Page<NoticeResponseDto> result = noticeService.getPublishedNotices(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("발행된 공지");
    }

    @Test
    @DisplayName("사용자용 상세 조회 성공")
    void getNoticeDetail_success() {
        // given
        Long noticeId = 1L;
        Notice notice = Notice.builder()
                .id(noticeId)
                .category(NoticeCategory.UPDATE)
                .title("공지 제목")
                .isPublished(true)
                .build();

        given(noticeRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(noticeId)).willReturn(Optional.of(notice));

        // when
        NoticeResponseDto response = noticeService.getNoticeDetail(noticeId);

        // then
        assertThat(response.getTitle()).isEqualTo("공지 제목");
    }

    @Test
    @DisplayName("사용자용 상세 조회 실패 - 존재하지 않거나 미발행")
    void getNoticeDetail_fail_notFound() {
        // given
        Long noticeId = 1L;
        given(noticeRepository.findByIdAndIsPublishedTrueAndDeletedAtIsNull(noticeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> noticeService.getNoticeDetail(noticeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOTICE_NOT_FOUND.getMessage());
    }
}

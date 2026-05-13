package com.dodo.dodoserver.domain.admin.notice.service;

import com.dodo.dodoserver.domain.admin.notice.dao.NoticeAdminRepository;
import com.dodo.dodoserver.domain.admin.notice.dto.NoticeRequestDto;
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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminNoticeServiceTest {

    @InjectMocks
    private AdminNoticeService adminNoticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeAdminRepository noticeAdminRepository;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job noticePublishJob;

    @Test
    @DisplayName("공지사항 등록 성공")
    void createNotice_success() {
        // given
        NoticeRequestDto requestDto = mock(NoticeRequestDto.class);
        given(requestDto.getCategory()).willReturn(NoticeCategory.UPDATE);
        given(requestDto.getTitle()).willReturn("제목");
        given(requestDto.getContent()).willReturn("내용");

        Notice notice = Notice.builder().id(1L).build();
        given(noticeRepository.save(any(Notice.class))).willReturn(notice);

        // when
        Long noticeId = adminNoticeService.createNotice(requestDto);

        // then
        assertThat(noticeId).isEqualTo(1L);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    void updateNotice_success() {
        // given
        Long noticeId = 1L;
        Notice notice = spy(Notice.builder()
                .id(noticeId)
                .category(NoticeCategory.EVENT)
                .title("기존 제목")
                .content("기존 내용")
                .build());

        NoticeRequestDto requestDto = mock(NoticeRequestDto.class);
        given(requestDto.getCategory()).willReturn(NoticeCategory.UPDATE);
        given(requestDto.getTitle()).willReturn("수정 제목");
        given(requestDto.getContent()).willReturn("수정 내용");

        given(noticeAdminRepository.findByIdWithDeleted(noticeId)).willReturn(Optional.of(notice));

        // when
        adminNoticeService.updateNotice(noticeId, requestDto);

        // then
        assertThat(notice.getCategory()).isEqualTo(NoticeCategory.UPDATE);
        assertThat(notice.getTitle()).isEqualTo("수정 제목");
        verify(notice).update(any(), any(), any());
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    void deleteNotice_success() {
        // given
        Long noticeId = 1L;
        Notice notice = Notice.builder().id(noticeId).build();
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when
        adminNoticeService.deleteNotice(noticeId);

        // then
        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("공지사항 발행 성공")
    void publishNotice_success() throws Exception {
        // given
        Long noticeId = 1L;
        Notice notice = Notice.builder()
                .id(noticeId)
                .title("공지 제목")
                .category(NoticeCategory.UPDATE)
                .isPublished(false)
                .build();
        
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when
        adminNoticeService.publishNotice(noticeId);

        // then
        assertThat(notice.isPublished()).isTrue();
        // 비동기 메소드 호출 자체는 spy나 별도 검증이 필요하지만, 로직 흐름상 publish() 호출 확인
    }

    @Test
    @DisplayName("공지사항 발행 실패 - 이미 발행됨")
    void publishNotice_fail_alreadyPublished() {
        // given
        Long noticeId = 1L;
        Notice notice = Notice.builder()
                .id(noticeId)
                .isPublished(true)
                .build();
        
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

        // when & then
        assertThatThrownBy(() -> adminNoticeService.publishNotice(noticeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_PUBLISHED_NOTICE.getMessage());
    }

    @Test
    @DisplayName("관리자용 목록 조회 성공")
    void getAllNotices_success() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Notice notice = Notice.builder().id(1L).category(NoticeCategory.UPDATE).title("제목").build();
        Page<Notice> page = new PageImpl<>(List.of(notice));
        
        given(noticeAdminRepository.findAllNoticesWithDeleted(pageable)).willReturn(page);

        // when
        Page<NoticeResponseDto> result = adminNoticeService.getAllNotices(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목");
    }
}

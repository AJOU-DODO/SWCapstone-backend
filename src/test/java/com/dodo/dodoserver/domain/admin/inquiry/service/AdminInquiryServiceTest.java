package com.dodo.dodoserver.domain.admin.inquiry.service;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryDetailResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.InquiryAnswerRequestDto;
import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryType;
import com.dodo.dodoserver.domain.inquiry.service.InquiryNotificationService;
import com.dodo.dodoserver.domain.user.entity.User;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminInquiryServiceTest {

    @InjectMocks
    private AdminInquiryService adminInquiryService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryNotificationService inquiryNotificationService;

    @Test
    @DisplayName("관리자용 문의 목록 조회 성공")
    void getInquiries_success() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).nickname("유저").build();
        Inquiry inquiry = Inquiry.builder()
                .id(1L)
                .user(user)
                .type(InquiryType.BUG)
                .title("제목")
                .status(InquiryStatus.PENDING)
                .build();
        Page<AdminInquiryResponseDto> page = new PageImpl<>(List.of(AdminInquiryResponseDto.from(inquiry)));

        given(inquiryRepository.findInquiriesForAdmin(InquiryStatus.PENDING, pageable)).willReturn(page);

        // when
        Page<AdminInquiryResponseDto> result = adminInquiryService.getInquiries(InquiryStatus.PENDING, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("문의 답변 등록 성공 및 알림 이벤트 발행 확인")
    void answerInquiry_success() {
        // given
        Long inquiryId = 1L;
        User user = User.builder().id(1L).email("user@test.com").build();
        Inquiry inquiry = Inquiry.builder()
                .id(inquiryId)
                .user(user)
                .status(InquiryStatus.PENDING)
                .build();
        InquiryAnswerRequestDto requestDto = new InquiryAnswerRequestDto("답변 내용");

        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));

        // when
        adminInquiryService.answerInquiry(inquiryId, requestDto);

        // then
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.COMPLETED);
        assertThat(inquiry.getAnswer()).isEqualTo("답변 내용");
        verify(inquiryNotificationService).sendInquiryAnswerNotification(inquiry);
    }
}

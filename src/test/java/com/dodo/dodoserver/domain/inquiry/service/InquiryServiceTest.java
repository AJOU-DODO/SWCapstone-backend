package com.dodo.dodoserver.domain.inquiry.service;

import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryRequestDto;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @InjectMocks
    private InquiryService inquiryService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("문의 등록 성공")
    void createInquiry_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        InquiryRequestDto requestDto = new InquiryRequestDto(InquiryType.BUG, "제목", "내용");
        Inquiry inquiry = Inquiry.builder()
                .id(1L)
                .user(user)
                .type(InquiryType.BUG)
                .title("제목")
                .content("내용")
                .status(InquiryStatus.PENDING)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);

        // when
        InquiryResponseDto result = inquiryService.createInquiry(userId, requestDto);

        // then
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getStatus()).isEqualTo(InquiryStatus.PENDING);
    }

    @Test
    @DisplayName("내 문의 목록 조회 성공")
    void getMyInquiries_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        PageRequest pageable = PageRequest.of(0, 10);
        Inquiry inquiry = Inquiry.builder()
                .id(1L)
                .user(user)
                .type(InquiryType.ACCOUNT)
                .title("문의")
                .build();
        Page<Inquiry> page = new PageImpl<>(List.of(inquiry));

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(inquiryRepository.findAllByUser(user, pageable)).willReturn(page);

        // when
        Page<InquiryResponseDto> result = inquiryService.getMyInquiries(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("문의");
    }

    @Test
    @DisplayName("문의 수정 실패 - 처리 완료된 문의")
    void updateInquiry_fail_completed() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        Long inquiryId = 1L;
        Inquiry inquiry = Inquiry.builder()
                .id(inquiryId)
                .user(user)
                .status(InquiryStatus.COMPLETED)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));
        InquiryRequestDto requestDto = new InquiryRequestDto(InquiryType.BUG, "수정제목", "수정내용");

        // when & then
        assertThatThrownBy(() -> inquiryService.updateInquiry(userId, inquiryId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CANNOT_MODIFY_COMPLETED_INQUIRY.getMessage());
    }

    @Test
    @DisplayName("문의 삭제 성공")
    void deleteInquiry_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        Long inquiryId = 1L;
        Inquiry inquiry = Inquiry.builder()
                .id(inquiryId)
                .user(user)
                .status(InquiryStatus.PENDING)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));

        // when
        inquiryService.deleteInquiry(userId, inquiryId);

        // then
        verify(inquiryRepository).delete(inquiry);
    }
}

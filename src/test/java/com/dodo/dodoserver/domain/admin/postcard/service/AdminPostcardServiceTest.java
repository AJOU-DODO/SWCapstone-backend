package com.dodo.dodoserver.domain.admin.postcard.service;

import com.dodo.dodoserver.domain.admin.postcard.dto.AdminPostcardDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static com.dodo.dodoserver.global.common.constants.NotificationConstants.DEFAULT_POSTCARD_DELETE_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPostcardServiceTest {

    @InjectMocks
    private AdminPostcardService adminPostcardService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostcardRepository postcardRepository;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private FcmService fcmService;

    @Test
    @DisplayName("신고된 엽서 목록 조회 성공")
    void getReportedPostcards_success() {
        // given
        AdminPostcardReportResponseDto responseDto = AdminPostcardReportResponseDto.builder()
                .postcardId(1L)
                .authorNickname("유저1")
                .build();
        Page<AdminPostcardReportResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto));
        given(reportRepository.findReportedPostcards(any(), any())).willReturn(page);

        // when
        Page<AdminPostcardReportResponseDto> result = adminPostcardService.getReportedPostcards(PageRequest.of(0, 10), "RECENT_REPORT");

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAuthorNickname()).isEqualTo("유저1");
    }

    @Test
    @DisplayName("관리자 전용 엽서 삭제 성공 - FCM 알림 및 소프트 삭제")
    void deletePostcardForAdmin_success() {
        // given
        User author = User.builder().id(1L).build();
        Postcard postcard = Postcard.builder().id(100L).originalAuthor(author).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminPostcardDeleteRequestDto requestDto = mock(AdminPostcardDeleteRequestDto.class);
        given(requestDto.getReason()).willReturn("삭제 사유");

        given(postcardRepository.findById(100L)).willReturn(Optional.of(postcard));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));

        // when
        adminPostcardService.deletePostcardForAdmin(100L, requestDto);

        // then
        verify(fcmService, times(1)).sendNotification(argThat(event -> 
                event.body().equals("삭제 사유")));
        verify(postcardRepository, times(1)).delete(postcard);
    }

    @Test
    @DisplayName("엽서 삭제 시 사유가 없으면 기본 사유로 알림 전송")
    void deletePostcard_useDefaultReason_whenReasonIsMissing() {
        // given
        User author = User.builder().id(1L).build();
        Postcard postcard = Postcard.builder().id(100L).originalAuthor(author).build();
        UserDevice device = UserDevice.builder().fcmToken("token").build();
        AdminPostcardDeleteRequestDto requestDto = new AdminPostcardDeleteRequestDto();

        given(postcardRepository.findById(100L)).willReturn(Optional.of(postcard));
        given(userDeviceRepository.findByUserId(1L)).willReturn(Collections.singletonList(device));

        // when
        adminPostcardService.deletePostcardForAdmin(100L, requestDto);

        // then
        verify(fcmService).sendNotification(argThat(event -> 
            event.body().equals(DEFAULT_POSTCARD_DELETE_REASON)
        ));
    }
}

package com.dodo.dodoserver.domain.admin.service;

import com.dodo.dodoserver.domain.admin.dao.SanctionHistoryRepository;
import com.dodo.dodoserver.domain.admin.dto.UserSanctionRequestDto;
import com.dodo.dodoserver.domain.admin.entity.SanctionHistory;
import com.dodo.dodoserver.domain.admin.entity.SanctionType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SanctionHistoryRepository sanctionHistoryRepository;

    @Test
    @DisplayName("유저 제재 처리 성공 - 7일 정지")
    void sanctionUser_success_sevenDays() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();
        UserSanctionRequestDto requestDto = new UserSanctionRequestDto();
        requestDto.setSanctionType(SanctionType.SEVEN_DAYS);
        requestDto.setReason("테스트 제재");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        adminService.sanctionUser(userId, requestDto);

        // then
        assertThat(user.getSanctionedUntil()).isNotNull();
        verify(sanctionHistoryRepository).save(any(SanctionHistory.class));
    }

    @Test
    @DisplayName("유저 제재 처리 성공 - 영구 정지")
    void sanctionUser_success_permanent() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();
        UserSanctionRequestDto requestDto = new UserSanctionRequestDto();
        requestDto.setSanctionType(SanctionType.PERMANENT);
        requestDto.setReason("영구 정지 테스트");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        adminService.sanctionUser(userId, requestDto);

        // then
        assertThat(user.getSanctionedUntil().getYear()).isEqualTo(9999);
        verify(sanctionHistoryRepository).save(any(SanctionHistory.class));
    }
}

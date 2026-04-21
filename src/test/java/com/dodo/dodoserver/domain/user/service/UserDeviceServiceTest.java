package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.entity.DeviceType;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeviceServiceTest {

    @InjectMocks
    private UserDeviceService userDeviceService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("기기 등록 성공 - 신규")
    void registerOrUpdateDevice_new() {
        // given
        String email = "test@example.com";
        DeviceRequestDto requestDto = new DeviceRequestDto("fcm-token", DeviceType.ANDROID);
        User user = User.builder().email(email).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userDeviceRepository.findByFcmToken("fcm-token")).willReturn(Optional.empty());

        // when
        userDeviceService.registerOrUpdateDevice(email, requestDto);

        // then
        verify(userDeviceRepository, times(1)).save(any(UserDevice.class));
    }

    @Test
    @DisplayName("기기 등록 성공 - 기존 갱신")
    void registerOrUpdateDevice_update() {
        // given
        String email = "test@example.com";
        DeviceRequestDto requestDto = new DeviceRequestDto("existing-token", DeviceType.IOS);
        User user = User.builder().email(email).build();
        UserDevice existingDevice = UserDevice.builder()
                .fcmToken("existing-token")
                .deviceType(DeviceType.ANDROID)
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userDeviceRepository.findByFcmToken("existing-token")).willReturn(Optional.of(existingDevice));

        // when
        userDeviceService.registerOrUpdateDevice(email, requestDto);

        // then
        assertThat(existingDevice.getDeviceType()).isEqualTo(DeviceType.IOS);
        assertThat(existingDevice.getUser()).isEqualTo(user);
        verify(userDeviceRepository, never()).save(any(UserDevice.class));
    }

    @Test
    @DisplayName("기기 등록 실패 - 유저 없음")
    void registerOrUpdateDevice_fail_userNotFound() {
        // given
        String email = "none@example.com";
        DeviceRequestDto requestDto = new DeviceRequestDto("fcm", DeviceType.ANDROID);
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDeviceService.registerOrUpdateDevice(email, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}

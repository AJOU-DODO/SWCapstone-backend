package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.user.dao.UserProfileRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.dto.OnboardRequestDto;
import com.dodo.dodoserver.domain.user.dto.ProfileUpdateRequestDto;
import com.dodo.dodoserver.domain.user.dto.UserProfileResponseDto;
import com.dodo.dodoserver.domain.user.entity.DeviceType;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserProfile;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserDeviceService userDeviceService;

    @Mock
    private UserInterestService userInterestService;

    @Test
    @DisplayName("닉네임 중복 확인 - 존재함")
    void existsByNickname_true() {
        // given
        String nickname = "테스터";
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // when
        boolean exists = userService.existsByNickname(nickname);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_success() {
        // given
        Long userId = 1L;
        ProfileUpdateRequestDto requestDto = new ProfileUpdateRequestDto("새닉네임", "http://new-image.com", "새소개");
        User user = User.builder().id(userId).email("test@example.com").nickname("옛날닉네임").build();
        UserProfile profile = UserProfile.builder().user(user).nickname("옛날닉네임").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));
        given(userRepository.existsByNickname("새닉네임")).willReturn(false);

        // when
        userService.updateProfile(userId, requestDto);

        // then
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(profile.getNickname()).isEqualTo("새닉네임");
        assertThat(profile.getProfileImageUrl()).isEqualTo("http://new-image.com");
        assertThat(profile.getBio()).isEqualTo("새소개");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 중복된 닉네임")
    void updateProfile_fail_duplicateNickname() {
        // given
        Long userId = 1L;
        ProfileUpdateRequestDto requestDto = new ProfileUpdateRequestDto("이미있는닉네임", null, null);
        User user = User.builder().id(userId).email("test@example.com").nickname("옛날닉네임").build();
        UserProfile profile = UserProfile.builder().user(user).nickname("옛날닉네임").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));
        given(userRepository.existsByNickname("이미있는닉네임")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(userId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("온보딩 성공")
    void onboard_success() {
        // given
        Long userId = 1L;
        OnboardRequestDto requestDto = new OnboardRequestDto(
                "신규유저", "fcm-token", "소개", "http://image.com", DeviceType.ANDROID, List.of(1L, 2L)
        );
        User user = User.builder().id(userId).email("new@example.com").isOnboarded(false).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("신규유저")).willReturn(false);

        // when
        userService.onboard(userId, requestDto);

        // then
        assertThat(user.isOnboarded()).isTrue();
        assertThat(user.getNickname()).isEqualTo("신규유저");
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(userDeviceService, times(1)).registerOrUpdateDevice(eq(userId), any(DeviceRequestDto.class));
        verify(userInterestService, times(1)).updateInterests(eq(userId), any());
    }

    @Test
    @DisplayName("온보딩 실패 - 이미 온보딩됨")
    void onboard_fail_alreadyOnboarded() {
        // given
        Long userId = 1L;
        OnboardRequestDto requestDto = new OnboardRequestDto("신규유저", "fcm-token", null, null, null, null);
        User user = User.builder().id(userId).email("old@example.com").isOnboarded(true).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.onboard(userId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_ONBOARDED.getMessage());
    }

    @Test
    @DisplayName("내 프로필 조회 성공")
    void getUserProfileById_success() {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        User user = User.builder()
                .id(userId)
                .email(email)
                .nickname("테스터")
                .role(Role.USER)
                .isOnboarded(true)
                .build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("테스터")
                .profileImageUrl("http://image.com")
                .bio("자기소개")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.of(profile));

        // when
        UserProfileResponseDto response = userService.getUserProfileById(userId);

        // then
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getBio()).isEqualTo("자기소개");
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 온보딩 미완료")
    void getUserProfileById_fail_notOnboarded() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).email("not-onboarded@example.com").isOnboarded(false).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.getUserProfileById(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ONBOARDING_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 사용자를 찾을 수 없음")
    void updateProfile_fail_userNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(userId, new ProfileUpdateRequestDto("닉네임", null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 프로필을 찾을 수 없음")
    void updateProfile_fail_profileNotFound() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUser(user)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(userId, new ProfileUpdateRequestDto("닉네임", null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_PROFILE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("온보딩 실패 - 닉네임 중복")
    void onboard_fail_duplicateNickname() {
        // given
        Long userId = 1L;
        OnboardRequestDto requestDto = new OnboardRequestDto("중복닉네임", "fcm", null, null, null, null);
        User user = User.builder().id(userId).email("new@example.com").isOnboarded(false).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.onboard(userId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 사용자를 찾을 수 없음")
    void getUserProfileById_fail_userNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserProfileById(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}

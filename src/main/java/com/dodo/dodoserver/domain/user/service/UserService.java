package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.dto.OnboardRequestDto;
import com.dodo.dodoserver.domain.user.dto.ProfileUpdateRequestDto;
import com.dodo.dodoserver.domain.user.dto.UserInterestRequestDto;
import com.dodo.dodoserver.domain.user.dto.UserProfileResponseDto;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserProfile;
import com.dodo.dodoserver.domain.user.dao.UserProfileRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 가입 및 온보딩 관리를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserDeviceService userDeviceService;
    private final UserInterestService userInterestService;

    /**
     * 특정 닉네임이 이미 존재하는지 확인
     */
    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 사용자의 프로필 정보를 업데이트
     */
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserProfile userProfile = userProfileRepository.findByUser(user)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        if (requestDto.getNickname() != null) {
            // 본인 닉네임이 아닌데 이미 존재하면 중복 예외
            if (!requestDto.getNickname().equals(user.getNickname()) &&
                userRepository.existsByNickname(requestDto.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.setNickname(requestDto.getNickname());
            userProfile.setNickname(requestDto.getNickname());
        }

        if (requestDto.getProfileImageUrl() != null) {
            userProfile.setProfileImageUrl(requestDto.getProfileImageUrl());
        }

        if (requestDto.getBio() != null) {
            userProfile.setBio(requestDto.getBio());
        }

        log.info("프로필 수정 완료: {}", userId);
    }

    /**
     * 구글 로그인 후 상세 정보(프로필, 기기 등)를 등록하고 가입
     * @param userId 사용자 ID
     * @param requestDto 온보딩 정보 (닉네임, 이미지, 소개, FCM 토큰, 관심 카테고리)
     */
    @Transactional
    public void onboard(Long userId, OnboardRequestDto requestDto) {
        // 유저 조회 및 상태 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isOnboarded()) {
            throw new BusinessException(ErrorCode.ALREADY_ONBOARDED);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // UserProfile 생성 및 저장
        UserProfile userProfile = UserProfile.builder()
            .user(user)
            .nickname(requestDto.getNickname())
            .profileImageUrl(requestDto.getProfileImageUrl())
            .bio(requestDto.getBio())
            .build();
        userProfileRepository.save(userProfile);

        // 기기 정보(FCM 토큰) 등록
        DeviceRequestDto deviceRequest = new DeviceRequestDto(
            requestDto.getFcmToken(),
            requestDto.getDeviceType()
        );
        userDeviceService.registerOrUpdateDevice(userId, deviceRequest);

        // 관심 카테고리 등록 (선택한 게 있을 경우)
        if (requestDto.getCategoryIds() != null && !requestDto.getCategoryIds().isEmpty()) {
            UserInterestRequestDto interestRequest = new UserInterestRequestDto(requestDto.getCategoryIds());
            userInterestService.updateInterests(userId, interestRequest);
        }

        // 온보딩 상태 완료 처리
        user.setOnboarded(true);
        user.setNickname(requestDto.getNickname());

        log.info("온보딩 성공: {}", userId);
    }

    /**
     * 사용자 상세 프로필 정보 조회 (ID 기반)
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfileById(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isOnboarded()) {
            throw new BusinessException(ErrorCode.ONBOARDING_REQUIRED);
        }

        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);

        return UserProfileResponseDto.from(user, userProfile);
    }

    /**
     * 사용자 상세 프로필 정보 조회 (이메일 기반)
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return getUserProfileById(user.getId());
    }
}

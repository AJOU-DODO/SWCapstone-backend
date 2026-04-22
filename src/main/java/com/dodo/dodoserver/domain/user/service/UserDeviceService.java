package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.user.dto.DeviceRequestDto;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 유저의 기기 및 FCM 토큰을 관리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    /**
     * 유저의 기기를 등록하거나 기존 기기의 토큰 정보를 갱신
     * @param userId 유저 ID (AccessToken에서 추출)
     * @param requestDto 등록할 기기 정보
     */
    @Transactional
    public void registerOrUpdateDevice(Long userId, DeviceRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 존재하는 토큰인지 확인
        userDeviceRepository.findByFcmToken(requestDto.getFcmToken())
                .ifPresentOrElse(
                        device -> {
                            device.setUser(user); // 주인이 바뀌었을 수도 있음
                            device.setDeviceType(requestDto.getDeviceType());
                            device.setLastActiveAt(LocalDateTime.now());
                            log.info("기존 기기 토큰 정보 갱신: {}", user.getId());
                        },
                        () -> {
                            UserDevice newDevice = UserDevice.builder()
                                    .user(user)
                                    .fcmToken(requestDto.getFcmToken())
                                    .deviceType(requestDto.getDeviceType())
                                    .build();
                            userDeviceRepository.save(newDevice);
                            log.info("신규 기기 토큰 등록: {}", user.getId());
                        }
                );
    }
}

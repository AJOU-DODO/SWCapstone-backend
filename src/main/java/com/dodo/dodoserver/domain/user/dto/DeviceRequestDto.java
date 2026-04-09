package com.dodo.dodoserver.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기기 등록 및 FCM 토큰 전송을 위한 DTO입니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeviceRequestDto {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String fcmToken;

    private String deviceType; // 예: "ANDROID", "IOS"
}

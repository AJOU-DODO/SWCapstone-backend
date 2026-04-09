package com.dodo.dodoserver.domain.user.dto;

import com.dodo.dodoserver.domain.user.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 최초 로그인 후 상세 정보(온보딩) 입력을 위한 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OnboardRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;

    private String profileImageUrl;

    @Size(max = 100, message = "자기소개는 100자 이내로 입력해주세요.")
    private String bio;

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String fcmToken;

    private DeviceType deviceType; // 수정됨
}

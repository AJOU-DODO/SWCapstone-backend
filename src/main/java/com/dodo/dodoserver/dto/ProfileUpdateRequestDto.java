package com.dodo.dodoserver.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 정보 수정을 위한 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProfileUpdateRequestDto {

    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;

    private String profileImageUrl;

    @Size(max = 100, message = "자기소개는 100자 이내로 입력해주세요.")
    private String bio;
}

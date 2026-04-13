package com.dodo.dodoserver.domain.user.dto;

import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 유저의 상세 프로필 정보를 반환하는 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private boolean isOnboarded;

    public static UserProfileResponseDto from(User user, UserProfile profile) {
        return UserProfileResponseDto.builder()
                .email(user.getEmail())
                .nickname(profile != null ? profile.getNickname() : user.getNickname())
                .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .isOnboarded(user.isOnboarded())
                .build();
    }
}

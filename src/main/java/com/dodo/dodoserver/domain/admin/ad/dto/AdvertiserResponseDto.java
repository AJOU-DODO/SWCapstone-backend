package com.dodo.dodoserver.domain.admin.ad.dto;

import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdvertiserResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private Integer allowedAdCount;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    public static AdvertiserResponseDto from(AdvertiserAuthority authority) {
        return AdvertiserResponseDto.builder()
                .userId(authority.getUser().getId())
                .email(authority.getUser().getEmail())
                .nickname(authority.getUser().getNickname())
                .allowedAdCount(authority.getAllowedAdCount())
                .expiredAt(authority.getExpiredAt())
                .createdAt(authority.getCreatedAt())
                .build();
    }
}

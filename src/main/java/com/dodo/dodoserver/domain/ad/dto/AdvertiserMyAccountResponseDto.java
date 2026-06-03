package com.dodo.dodoserver.domain.ad.dto;

import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdvertiserMyAccountResponseDto {
    private Integer allowedAdCount;
    private Long currentAdCount;
    private Long pendingAdCount;
    private Long remainingAdCount;
    private LocalDateTime expiredAt;
    private Boolean isExpired;

    public static AdvertiserMyAccountResponseDto of(AdvertiserAuthority authority, long currentAdCount, long pendingAdCount) {
        long totalUsed = currentAdCount + pendingAdCount;
        long remaining = Math.max(0, authority.getAllowedAdCount() - totalUsed);
        boolean isExpired = authority.getExpiredAt().isBefore(LocalDateTime.now());

        return AdvertiserMyAccountResponseDto.builder()
                .allowedAdCount(authority.getAllowedAdCount())
                .currentAdCount(currentAdCount)
                .pendingAdCount(pendingAdCount)
                .remainingAdCount(remaining)
                .expiredAt(authority.getExpiredAt())
                .isExpired(isExpired)
                .build();
    }
}

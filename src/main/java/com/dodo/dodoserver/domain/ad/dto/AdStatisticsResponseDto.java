package com.dodo.dodoserver.domain.ad.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdStatisticsResponseDto {
    private Long nestId;
    private String title;
    private Long impressions;
    private Long clicks;
    private LocalDateTime expiredAt;

    public static AdStatisticsResponseDto of(Long nestId, String title, Long impressions, Long clicks, LocalDateTime expiredAt) {
        return AdStatisticsResponseDto.builder()
                .nestId(nestId)
                .title(title)
                .impressions(impressions)
                .clicks(clicks)
                .expiredAt(expiredAt)
                .build();
    }
}

package com.dodo.dodoserver.domain.admin.ad.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdNestAdminResponseDto {
    private Long id;
    private String title;
    private String advertiserNickname;
    private LocalDateTime expiredAt;
    private Integer priorityScore;
    private Long impressions;
    private Long clicks;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static AdNestAdminResponseDto of(Nest nest, LocalDateTime expiredAt, Integer priorityScore, Long impressions, Long clicks) {
        return AdNestAdminResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .advertiserNickname(nest.getCreator().getNickname())
                .expiredAt(expiredAt)
                .priorityScore(priorityScore)
                .impressions(impressions)
                .clicks(clicks)
                .createdAt(nest.getCreatedAt())
                .deletedAt(nest.getDeletedAt())
                .build();
    }
}

package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NestSummaryResponseDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private Long likeCount;
    private Double distance;
    private boolean isAd;
    private boolean isUnlocked;

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked, Long likeCount, Double distance) {
        String thumbnail = nest.getImages().isEmpty() ? null : nest.getImages().get(0).getImageUrl();
        return NestSummaryResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .thumbnailUrl(thumbnail)
                .likeCount(likeCount != null ? likeCount : 0L)
                .distance(distance)
                .isAd(nest.isAd())
                .isUnlocked(isUnlocked)
                .build();
    }

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked) {
        return from(nest, isUnlocked, 0L, null);
    }
}


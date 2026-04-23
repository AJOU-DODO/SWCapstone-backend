package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NestSummaryResponseDto {
    private Long id;
    private String content;
    private String thumbnailUrl;
    private Long likeCount;
    private Double distance;
    private boolean isAd;
    private boolean isUnlocked;

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked, Long likeCount, Double distance) {
        String thumbnail = nest.getImages().isEmpty() ? null : nest.getImages().get(0).getImageUrl();

        String displayContent = nest.getContent();
        if (displayContent != null) {
            int newlineIndex = displayContent.indexOf('\n');
            if (newlineIndex != -1) {
                displayContent = displayContent.substring(0, newlineIndex);
            }
        }

        return NestSummaryResponseDto.builder()
                .id(nest.getId())
                .content(displayContent)
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


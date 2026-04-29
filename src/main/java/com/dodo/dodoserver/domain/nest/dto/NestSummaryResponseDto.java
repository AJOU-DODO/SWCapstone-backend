package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.*;

import java.util.Collections;
import java.util.List;

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
    private List<String> categoryNames;
    private boolean isAd;
    private boolean isUnlocked;
    private boolean hasPostcard;
    private Long postcardId;

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked, Long likeCount, Double distance, List<String> categoryNames, Long postcardId) {
        String thumbnail = nest.getImages().isEmpty() ? null : nest.getImages().get(0).getImageUrl();

        String displayContent = nest.getContent(); // 해금 전 콘텐츠 필터링
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
                .categoryNames(categoryNames)
                .isAd(nest.isAd())
                .isUnlocked(isUnlocked)
                .hasPostcard(postcardId != null)
                .postcardId(postcardId)
                .build();
    }

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked) {
        return from(nest, isUnlocked, 0L, null, Collections.emptyList(), null);
    }
}


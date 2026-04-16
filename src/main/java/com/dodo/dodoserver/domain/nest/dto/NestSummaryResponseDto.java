package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestImage;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NestSummaryResponseDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private boolean isAd;
    private boolean isUnlocked;

    public static NestSummaryResponseDto from(Nest nest, boolean isUnlocked) {
        String thumbnail = nest.getImages().isEmpty() ? null : nest.getImages().get(0).getImageUrl();
        return NestSummaryResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .thumbnailUrl(thumbnail)
                .isAd(nest.isAd())
                .isUnlocked(isUnlocked)
                .build();
    }
}

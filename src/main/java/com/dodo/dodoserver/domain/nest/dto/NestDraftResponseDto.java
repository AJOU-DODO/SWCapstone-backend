package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NestDraftResponseDto {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String title;
    private String content;
    private Integer unlockRadius;
    private List<Long> categoryIds;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NestDraftResponseDto from(NestDraft draft) {
        return NestDraftResponseDto.builder()
                .id(draft.getId())
                .latitude(draft.getLatitude())
                .longitude(draft.getLongitude())
                .title(draft.getTitle())
                .content(draft.getContent())
                .unlockRadius(draft.getUnlockRadius())
                .categoryIds(draft.getCategoryIds())
                .imageUrls(draft.getImageUrls())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}

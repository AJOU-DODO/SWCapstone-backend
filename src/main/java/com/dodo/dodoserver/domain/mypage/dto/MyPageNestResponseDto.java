package com.dodo.dodoserver.domain.mypage.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageNestResponseDto {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private boolean isUnlocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MyPageNestResponseDto from(Nest nest, boolean isUnlocked) {
        String thumbnail = nest.getImages().isEmpty() ? null : nest.getImages().get(0).getImageUrl();
        
        return MyPageNestResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .content(nest.getContent())
                .thumbnailUrl(thumbnail)
                .isUnlocked(isUnlocked)
                .createdAt(nest.getCreatedAt())
                .updatedAt(nest.getUpdatedAt())
                .build();
    }
}

package com.dodo.dodoserver.domain.postcard.dto;

import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostcardResponseDto {
    private Long id;
    private Long originalAuthorId;
    private String originalAuthorNickname;
    private String imageUrl;
    private String content;
    private boolean isShared;
    private boolean isExchanged;
    private LocalDateTime createdAt;

    public static PostcardResponseDto from(Postcard postcard) {
        return PostcardResponseDto.builder()
                .id(postcard.getId())
                .originalAuthorId(postcard.getOriginalAuthor().getId())
                .originalAuthorNickname(postcard.getOriginalAuthor().getNickname())
                .imageUrl(postcard.getImageUrl())
                .content(postcard.getContent())
                .isShared(postcard.isShared())
                .isExchanged(postcard.isExchanged())
                .createdAt(postcard.getCreatedAt())
                .build();
    }
}

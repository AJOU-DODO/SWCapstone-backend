package com.dodo.dodoserver.domain.postcard.dto;

import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReactionType;
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
    private boolean isMine; // 내가 원작자인지 여부
    private LocalDateTime createdAt;
    private PostcardReactionType reactionType;

    public static PostcardResponseDto from(Postcard postcard, Long currentUserId) {
        return PostcardResponseDto.builder()
                .id(postcard.getId())
                .originalAuthorId(postcard.getOriginalAuthor().getId())
                .originalAuthorNickname(postcard.getOriginalAuthor().getNickname())
                .imageUrl(postcard.getImageUrl())
                .content(postcard.getContent())
                .isShared(postcard.isShared())
                .isExchanged(postcard.isExchanged())
                .isMine(postcard.getOriginalAuthor().getId().equals(currentUserId))
                .createdAt(postcard.getCreatedAt())
                .build();
    }

    public static PostcardResponseDto from(Postcard postcard, PostcardReactionType reactionType, Long currentUserId) {
        return PostcardResponseDto.builder()
                .id(postcard.getId())
                .originalAuthorId(postcard.getOriginalAuthor().getId())
                .originalAuthorNickname(postcard.getOriginalAuthor().getNickname())
                .imageUrl(postcard.getImageUrl())
                .content(postcard.getContent())
                .isShared(postcard.isShared())
                .isExchanged(postcard.isExchanged())
                .isMine(postcard.getOriginalAuthor().getId().equals(currentUserId))
                .createdAt(postcard.getCreatedAt())
                .reactionType(reactionType)
                .build();
    }
}

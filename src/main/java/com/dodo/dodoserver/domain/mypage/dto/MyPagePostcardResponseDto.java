package com.dodo.dodoserver.domain.mypage.dto;

import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPagePostcardResponseDto {
    private Long id;
    private String imageUrl;
    private String content;
    private String authorNickname;
    private boolean isMine;
    private LocalDateTime createdAt;

    public static MyPagePostcardResponseDto from(Postcard postcard, Long currentUserId) {
        return MyPagePostcardResponseDto.builder()
                .id(postcard.getId())
                .imageUrl(postcard.getImageUrl())
                .content(postcard.getContent())
                .authorNickname(postcard.getOriginalAuthor().getNickname())
                .isMine(postcard.getOriginalAuthor().getId().equals(currentUserId))
                .createdAt(postcard.getCreatedAt())
                .build();
    }
}

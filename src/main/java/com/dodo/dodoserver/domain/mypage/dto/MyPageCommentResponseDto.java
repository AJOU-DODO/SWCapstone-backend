package com.dodo.dodoserver.domain.mypage.dto;

import com.dodo.dodoserver.domain.nest.entity.NestComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageCommentResponseDto {
    private Long id;
    private Long nestId;
    private String nestTitle;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;

    public static MyPageCommentResponseDto from(NestComment comment) {
        return MyPageCommentResponseDto.builder()
                .id(comment.getId())
                .nestId(comment.getNest().getId())
                .nestTitle(comment.getNest().getTitle())
                .content(comment.getContent())
                .authorNickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}

package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.NestComment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private Long likeCount;
    private boolean isLiked;
    private List<CommentResponseDto> children;

    public static CommentResponseDto from(NestComment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUser().getNickname())
                .profileImageUrl(null) // UserProfile 연동 시 추가 가능
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount())
                .isLiked(false)
                .children(comment.getChildren().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}

package com.dodo.dodoserver.domain.admin.nest.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminCommentResponseDto {
    private Long commentId;
    private Long parentId;
    private Long authorId;
    private String authorNickname;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private long pendingReportCount;
    private long likeCount;
    private List<AdminCommentResponseDto> children;
}

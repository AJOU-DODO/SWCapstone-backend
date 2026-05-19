package com.dodo.dodoserver.domain.admin.nest.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminCommentResponseDto {
    private Long commentId;
    private Long parentId;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private long pendingReportCount;
}

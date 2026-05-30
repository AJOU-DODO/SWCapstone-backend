package com.dodo.dodoserver.domain.admin.nest.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminNestDetailResponseDto {
    private Long nestId;
    private String title;
    private String content;
    private Long authorId;
    private String authorNickname;
    private String profileImageUrl;
    private Double latitude;
    private Double longitude;
    private List<String> imageUrls;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private LocalDateTime createdAt;
    private LocalDateTime firstReportedAt;
    private LocalDateTime lastReportedAt;
    private long likeCount;
    private long dislikeCount;
    private boolean isDeleted;
}

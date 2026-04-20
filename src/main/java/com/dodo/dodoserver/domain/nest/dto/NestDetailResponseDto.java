package com.dodo.dodoserver.domain.nest.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NestDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private Integer unlockRadius;
    private Long viewCount;
    private boolean isAd;
    private LocalDateTime createdAt;
    
    // 작성자 정보
    private String creatorNickname;
    private String creatorProfileImageUrl;

    // 카테고리 및 이미지
    private List<String> categoryNames;
    private List<String> imageUrls;

    // 인터랙션 정보
    private long likeCount;
    private long dislikeCount;
    private boolean isUnlocked;
}

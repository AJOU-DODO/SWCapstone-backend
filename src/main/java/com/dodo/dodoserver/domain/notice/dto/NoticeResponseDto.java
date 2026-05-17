package com.dodo.dodoserver.domain.notice.dto;

import com.dodo.dodoserver.domain.notice.entity.Notice;
import com.dodo.dodoserver.domain.notice.entity.NoticeCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponseDto {
    private Long id;
    private NoticeCategory category;
    private String categoryDescription;
    private String title;
    private String content;
    private boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static NoticeResponseDto from(Notice notice) {
        return NoticeResponseDto.builder()
                .id(notice.getId())
                .category(notice.getCategory())
                .categoryDescription(notice.getCategory().getDescription())
                .title(notice.getTitle())
                .content(notice.getContent())
                .isPublished(notice.isPublished())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .deletedAt(notice.getDeletedAt())
                .build();
    }
}

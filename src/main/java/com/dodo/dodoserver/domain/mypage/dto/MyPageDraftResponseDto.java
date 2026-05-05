package com.dodo.dodoserver.domain.mypage.dto;

import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageDraftResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MyPageDraftResponseDto from(NestDraft draft) {
        return MyPageDraftResponseDto.builder()
                .id(draft.getId())
                .title(draft.getTitle())
                .content(draft.getContent())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}

package com.dodo.dodoserver.domain.admin.notice.dto;

import com.dodo.dodoserver.domain.notice.entity.NoticeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeRequestDto {
    @NotNull(message = "카테고리는 필수입니다.")
    private NoticeCategory category;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}

package com.dodo.dodoserver.domain.nest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {
    @NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    private String content;

    private Long parentId; // 대댓글일 경우 부모 댓글 ID
}

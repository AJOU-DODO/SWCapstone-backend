package com.dodo.dodoserver.domain.nest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequestDto {
    @NotBlank(message = "수정할 내용은 비어있을 수 없습니다.")
    private String content;
}

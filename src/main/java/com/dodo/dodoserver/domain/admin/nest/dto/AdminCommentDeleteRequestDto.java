package com.dodo.dodoserver.domain.admin.nest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자용 댓글 삭제 요청 DTO")
public class AdminCommentDeleteRequestDto {
    @Schema(description = "삭제 사유 (미입력 시 기본 문구 사용)", example = "부적절한 내용 포함")
    private String reason;
}

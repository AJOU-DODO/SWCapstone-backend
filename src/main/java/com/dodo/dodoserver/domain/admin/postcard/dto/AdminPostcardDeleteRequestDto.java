package com.dodo.dodoserver.domain.admin.postcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "관리자용 엽서 삭제 요청 DTO")
public class AdminPostcardDeleteRequestDto {
    @Schema(description = "삭제 사유 (미입력 시 기본 문구 사용)", example = "부적절한 내용 포함")
    private String reason;
}

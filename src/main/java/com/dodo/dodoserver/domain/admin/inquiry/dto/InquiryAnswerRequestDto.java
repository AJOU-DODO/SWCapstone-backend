package com.dodo.dodoserver.domain.admin.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InquiryAnswerRequestDto {
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String answer;
}

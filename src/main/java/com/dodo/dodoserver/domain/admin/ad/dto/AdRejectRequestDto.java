package com.dodo.dodoserver.domain.admin.ad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdRejectRequestDto {
    @NotBlank(message = "반려 사유는 필수입니다.")
    private String rejectReason;
}

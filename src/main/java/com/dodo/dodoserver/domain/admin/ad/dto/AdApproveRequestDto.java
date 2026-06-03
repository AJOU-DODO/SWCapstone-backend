package com.dodo.dodoserver.domain.admin.ad.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdApproveRequestDto {
    @NotNull(message = "광고 만료일은 필수입니다.")
    private LocalDateTime expiredAt;

    private Integer priorityScore = 0;
}

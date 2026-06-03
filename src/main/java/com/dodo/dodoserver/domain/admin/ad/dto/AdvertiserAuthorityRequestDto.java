package com.dodo.dodoserver.domain.admin.ad.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdvertiserAuthorityRequestDto {
    @NotNull(message = "허용 광고 개수는 필수입니다.")
    @Min(value = 1, message = "최소 1개 이상의 광고를 허용해야 합니다.")
    private Integer allowedAdCount;

    @NotNull(message = "만료일은 필수입니다.")
    private LocalDateTime expiredAt;
}

package com.dodo.dodoserver.domain.admin.dto;

import com.dodo.dodoserver.domain.admin.entity.SanctionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSanctionRequestDto {
    @NotNull(message = "제재 타입은 필수입니다.")
    private SanctionType sanctionType;

    @NotBlank(message = "제재 사유는 필수입니다.")
    private String reason;
}

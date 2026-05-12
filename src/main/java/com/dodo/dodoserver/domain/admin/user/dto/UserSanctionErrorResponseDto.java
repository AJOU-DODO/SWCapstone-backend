package com.dodo.dodoserver.domain.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSanctionErrorResponseDto {
    private LocalDateTime sanctionedUntil;
    private String reason;
}

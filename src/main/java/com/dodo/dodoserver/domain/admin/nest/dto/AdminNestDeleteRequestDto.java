package com.dodo.dodoserver.domain.admin.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNestDeleteRequestDto {
    private String reason;
}

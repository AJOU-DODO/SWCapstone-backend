package com.dodo.dodoserver.domain.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NestPinResponseDto {
    private Long id;
    private Double latitude;
    private Double longitude;
}

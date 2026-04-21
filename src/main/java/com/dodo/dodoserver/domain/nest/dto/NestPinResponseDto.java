package com.dodo.dodoserver.domain.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NestPinResponseDto {
    private Long id;
    private Double latitude;
    private Double longitude;

    public static NestPinResponseDto from(NestPinProjection projection) {
        return NestPinResponseDto.builder()
                .id(projection.getId())
                .latitude(projection.getLatitude())
                .longitude(projection.getLongitude())
                .build();
    }
}

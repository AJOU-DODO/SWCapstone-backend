package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NestSimpleResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NestSimpleResponseDto from(Nest nest) {
        return NestSimpleResponseDto.builder()
                .id(nest.getId())
                .createdAt(nest.getCreatedAt())
                .updatedAt(nest.getUpdatedAt())
                .build();
    }
}

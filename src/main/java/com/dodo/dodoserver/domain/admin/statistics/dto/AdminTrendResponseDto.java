package com.dodo.dodoserver.domain.admin.statistics.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTrendResponseDto {
    private LocalDate date;
    private Long nestCount;
    private Long commentCount;
    private Long postcardCount;

    public static AdminTrendResponseDto empty(LocalDate date) {
        return AdminTrendResponseDto.builder()
                .date(date)
                .nestCount(0L)
                .commentCount(0L)
                .postcardCount(0L)
                .build();
    }
}

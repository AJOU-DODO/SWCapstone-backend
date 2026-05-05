package com.dodo.dodoserver.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageStatisticsResponseDto {
    private long nestCount;
    private long commentCount;
    private long postcardCount;

    public static MyPageStatisticsResponseDto of(long nestCount, long commentCount, long postcardCount) {
        return MyPageStatisticsResponseDto.builder()
                .nestCount(nestCount)
                .commentCount(commentCount)
                .postcardCount(postcardCount)
                .build();
    }
}

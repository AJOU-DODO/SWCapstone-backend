package com.dodo.dodoserver.domain.postcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostcardExchangeCheckResponseDto {
    private boolean canExchange;
    private int remainingCount;
    private String reason;
}

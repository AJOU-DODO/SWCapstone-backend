package com.dodo.dodoserver.domain.admin.statistics.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPostcardRatioResponseDto {
    private Long totalGenerated;
    private Long totalDelivered;
    private Double deliveryRatio;
}

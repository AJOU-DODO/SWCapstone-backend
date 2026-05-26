package com.dodo.dodoserver.domain.admin.statistics.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSummaryResponseDto {
    private Long totalNests;
    private Long todayNests;
    private Long totalComments;
    private Long todayComments;
    private Long totalPostcards;
    private Long todayPostcards;
}

package com.dodo.dodoserver.domain.admin.report.dto;

import com.dodo.dodoserver.domain.report.entity.ReportType;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportDetailResponseDto {
    private ReportType targetType;
    private Long targetId;
    private Map<String, Long> stats;
    private List<String> otherReportContents;
}

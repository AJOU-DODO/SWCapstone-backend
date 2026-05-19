package com.dodo.dodoserver.domain.report.dto;

import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportRequestDto {

    @NotNull(message = "신고 타입은 필수입니다.")
    private ReportType reportType;

    @NotNull(message = "신고 대상 ID는 필수입니다.")
    private Long targetId;

    @NotNull(message = "신고 사유는 필수입니다.")
    private ReportReason reason;

    private String content;
}

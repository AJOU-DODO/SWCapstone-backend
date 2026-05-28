package com.dodo.dodoserver.domain.admin.report.dto;

import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자용 신고 상태 일괄 변경 요청 DTO")
public class AdminReportStatusUpdateRequestDto {

    @NotNull(message = "대상 타입을 지정해야 합니다.")
    @Schema(description = "신고 대상 타입 (NEST, COMMENT, POSTCARD)", example = "NEST")
    private ReportType targetType;

    @NotNull(message = "대상 ID를 지정해야 합니다.")
    @Schema(description = "신고 대상 콘텐츠의 ID", example = "1")
    private Long targetId;

    @NotNull(message = "변경할 상태를 지정해야 합니다.")
    @Schema(description = "변경할 상태 (PROCESSED: 처리 완료, REJECTED: 반려)", example = "PROCESSED")
    private ReportStatus newStatus;
}

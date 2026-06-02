package com.dodo.dodoserver.domain.admin.report.dto;

import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminPostcardReportResponseDto {
    private Long postcardId;
    private Long authorId;
    private String authorNickname;
    private String content;
    private String imageUrl;
    private LocalDateTime firstReportedAt;
    private LocalDateTime lastReportedAt;
    private long reportCount;
    private List<ReportReason> reasons;
    private ReportStatus status;
}

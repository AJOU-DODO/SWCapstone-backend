package com.dodo.dodoserver.domain.admin.nest.dto;

import com.dodo.dodoserver.domain.report.entity.ReportReason;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdminNestResponseDto {
    private Long nestId;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;
    private long likeCount;
    private long commentCount;
    private long reportCount;
    private List<ReportReason> reasons;
}

package com.dodo.dodoserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("대기"),
    PROCESSED("처리 완료"),
    REJECTED("반려");

    private final String description;
}

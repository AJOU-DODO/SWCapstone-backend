package com.dodo.dodoserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    NEST("둥지"),
    COMMENT("댓글"),
    POSTCARD("엽서");

    private final String description;
}

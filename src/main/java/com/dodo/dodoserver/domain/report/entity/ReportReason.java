package com.dodo.dodoserver.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    ABUSE("욕설 및 비방"),
    SPAM("도배성 게시글"),
    ADVERTISEMENT("영리목적/광고"),
    OTHER("기타");

    private final String description;
}

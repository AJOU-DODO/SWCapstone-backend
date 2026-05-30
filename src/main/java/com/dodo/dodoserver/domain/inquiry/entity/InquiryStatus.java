package com.dodo.dodoserver.domain.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {
    PENDING("대기"),
    COMPLETED("처리 완료");

    private final String description;
}

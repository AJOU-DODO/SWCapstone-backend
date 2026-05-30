package com.dodo.dodoserver.domain.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryType {
    ACCOUNT("계정"),
    BUG("오류/버그"),
    SUGGESTION("기능 건의"),
    BUSINESS("비즈니스");

    private final String description;
}

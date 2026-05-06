package com.dodo.dodoserver.domain.admin.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SanctionType {
    SEVEN_DAYS(7, "7일 정지"),
    THIRTY_DAYS(30, "30일 정지"),
    PERMANENT(null, "영구 정지");

    private final Integer days;
    private final String description;
}

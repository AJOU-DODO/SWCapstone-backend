package com.dodo.dodoserver.domain.admin.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public enum SanctionType {
    SEVEN_DAYS(7, "7일 정지"),
    THIRTY_DAYS(30, "30일 정지"),
    PERMANENT(null, "영구 정지");

    private final Integer days;
    private final String description;

    public LocalDateTime calculateEndedAt() {
        if (this == PERMANENT) {
            // 영구 정지: 9999년 12월 31일
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
        return LocalDateTime.now().plusDays(this.days);
    }
}

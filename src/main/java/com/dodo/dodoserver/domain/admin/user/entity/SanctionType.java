package com.dodo.dodoserver.domain.admin.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public enum SanctionType {
    SEVEN_DAYS(7, "7일 정지"),
    THIRTY_DAYS(30, "30일 정지"),
    PERMANENT(null, "영구 정지"),
    LIFTED(0, "제재 해제");

    private final Integer days;
    private final String description;

    public LocalDateTime calculateEndedAt() {
        if (this == PERMANENT) {
            // 영구 정지: 타임존 변환 시 MySQL DATETIME 범위를 넘지 않도록 안전하게 설정
            return LocalDateTime.of(9999, 1, 1, 0, 0, 0);
        }
        if (this == LIFTED) {
            return LocalDateTime.now();
        }
        return LocalDateTime.now().plusDays(this.days);
    }
}

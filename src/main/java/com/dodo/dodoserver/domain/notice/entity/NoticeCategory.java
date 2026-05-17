package com.dodo.dodoserver.domain.notice.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeCategory {
    UPDATE("업데이트 공지"),
    EVENT("이벤트 공지"),
    POLICY("정책 변경 공지");

    private final String description;
}

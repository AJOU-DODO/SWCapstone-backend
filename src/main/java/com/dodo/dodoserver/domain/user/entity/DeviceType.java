package com.dodo.dodoserver.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 접속 기기의 운영체제 및 플랫폼 유형을 정의
 */
@Getter
@RequiredArgsConstructor
public enum DeviceType {
    ANDROID("Android OS"),
    IOS("iOS"),
    WEB("Web"),
    UNKNOWN("Unknown");

    private final String description;
}

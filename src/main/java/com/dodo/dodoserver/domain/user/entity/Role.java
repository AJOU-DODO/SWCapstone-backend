package com.dodo.dodoserver.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER"),
    ADVERTISER("ROLE_ADVERTISER"),
    ADMIN("ROLE_ADMIN");

    private final String key;
}

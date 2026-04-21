package com.dodo.dodoserver.domain.nest.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionType {
    LIKE("LIKE"),
    DISLIKE("DISLIKE");

    private final String value;
}

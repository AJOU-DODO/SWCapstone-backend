package com.dodo.dodoserver.domain.postcard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostcardReactionType {
    TOUCHED("감동이에요"),
    AWESOME("멋져요"),
    HAPPY("기뻐요"),
    BEST("최고에요");

    private final String value;
}

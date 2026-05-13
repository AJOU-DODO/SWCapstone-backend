package com.dodo.dodoserver.domain.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeCreateResponseDto {
    private Long id;

    public static NoticeCreateResponseDto from(Long id) {
        return new NoticeCreateResponseDto(id);
    }
}

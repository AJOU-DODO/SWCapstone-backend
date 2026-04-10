package com.dodo.dodoserver.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 유저의 관심 카테고리 리스트 수정을 위한 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserInterestRequestDto {

    @NotEmpty(message = "최소 하나 이상의 카테고리를 선택해야 합니다.")
    private List<Long> categoryIds;
}

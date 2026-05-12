package com.dodo.dodoserver.domain.admin.category.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryOrderUpdateRequestDto {

    @NotEmpty(message = "순서 변경 정보는 필수입니다.")
    @Valid
    private List<CategoryOrderDto> orders;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class CategoryOrderDto {
        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Long id;

        @NotNull(message = "순서는 필수입니다.")
        private Integer sortOrder;
    }
}

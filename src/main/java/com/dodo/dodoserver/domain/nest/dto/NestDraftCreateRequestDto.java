package com.dodo.dodoserver.domain.nest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NestDraftCreateRequestDto {

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
    private String title;

    private String content;

    @Min(value = 0, message = "해금 반경은 0 이상이어야 합니다.")
    private Integer unlockRadius;

    private List<Long> categoryIds;

    @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다.")
    private List<String> imageUrls;
}

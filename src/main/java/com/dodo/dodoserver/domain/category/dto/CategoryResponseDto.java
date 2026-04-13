package com.dodo.dodoserver.domain.category.dto;

import com.dodo.dodoserver.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public static CategoryResponseDto from(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getCreatedAt()
        );
    }
}

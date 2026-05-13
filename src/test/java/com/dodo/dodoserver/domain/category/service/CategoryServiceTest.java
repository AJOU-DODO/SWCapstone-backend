package com.dodo.dodoserver.domain.category.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void getAllCategories_success() {
        // given
        Category cat1 = Category.builder().id(1L).name("운동").sortOrder(1).build();
        Category cat2 = Category.builder().id(2L).name("공부").sortOrder(2).build();
        given(categoryRepository.findAllByDeletedAtIsNullOrderBySortOrderAsc()).willReturn(List.of(cat1, cat2));

        // when
        List<CategoryResponseDto> categories = categoryService.getAllCategories();

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getName()).isEqualTo("운동");
        assertThat(categories.get(1).getName()).isEqualTo("공부");
    }
}

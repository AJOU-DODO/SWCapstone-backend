package com.dodo.dodoserver.domain.category.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.dto.CategoryRequestDto;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() {
        // given
        CategoryRequestDto requestDto = new CategoryRequestDto("운동");
        Category category = Category.builder()
                .id(1L)
                .name("운동")
                .createdAt(LocalDateTime.now())
                .build();

        given(categoryRepository.findByName("운동")).willReturn(Optional.empty());
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);

        // then
        assertThat(responseDto.getName()).isEqualTo("운동");
        assertThat(responseDto.getId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 중복된 이름")
    void createCategory_fail_duplicateName() {
        // given
        CategoryRequestDto requestDto = new CategoryRequestDto("운동");
        Category existingCategory = Category.builder().name("운동").build();
        given(categoryRepository.findByName("운동")).willReturn(Optional.of(existingCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_CATEGORY_NAME.getMessage());
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void getAllCategories_success() {
        // given
        Category cat1 = Category.builder().id(1L).name("운동").build();
        Category cat2 = Category.builder().id(2L).name("공부").build();
        given(categoryRepository.findAll()).willReturn(List.of(cat1, cat2));

        // when
        List<CategoryResponseDto> categories = categoryService.getAllCategories();

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getName()).isEqualTo("운동");
        assertThat(categories.get(1).getName()).isEqualTo("공부");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_success() {
        // given
        Long categoryId = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("독서");
        Category category = Category.builder().id(categoryId).name("운동").build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.findByName("독서")).willReturn(Optional.empty());

        // when
        CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, requestDto);

        // then
        assertThat(responseDto.getName()).isEqualTo("독서");
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 존재하지 않는 카테고리")
    void updateCategory_fail_notFound() {
        // given
        Long categoryId = 99L;
        CategoryRequestDto requestDto = new CategoryRequestDto("독서");
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 중복된 이름")
    void updateCategory_fail_duplicateName() {
        // given
        Long categoryId = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("중복이름");
        Category category = Category.builder().id(categoryId).name("기존이름").build();
        Category existingCategory = Category.builder().id(2L).name("중복이름").build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.findByName("중복이름")).willReturn(Optional.of(existingCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_CATEGORY_NAME.getMessage());
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 카테고리")
    void deleteCategory_fail_notFound() {
        // given
        Long categoryId = 99L;
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}

package com.dodo.dodoserver.domain.admin.category.service;

import com.dodo.dodoserver.domain.admin.category.dao.CategoryAdminRepository;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryRequestDto;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;
import com.dodo.dodoserver.domain.admin.category.dto.CategoryOrderUpdateRequestDto;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryAdminRepository categoryAdminRepository;

    @Test
    @DisplayName("관리자용 카테고리 목록 조회 성공")
    void getCategories_success() {
        // given
        AdminCategoryResponseDto dto = AdminCategoryResponseDto.builder()
                .id(1L).name("운동").sortOrder(0).nestCount(5L).build();
        given(categoryAdminRepository.findAllAdminCategories(true, "nestCount")).willReturn(List.of(dto));

        // when
        List<AdminCategoryResponseDto> result = adminCategoryService.getCategories(true, "nestCount");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("운동");
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() {
        // given
        AdminCategoryRequestDto requestDto = new AdminCategoryRequestDto("운동");
        Category category = Category.builder()
                .id(1L)
                .name("운동")
                .sortOrder(1)
                .build();
        
        given(categoryRepository.findByName("운동")).willReturn(Optional.empty());
        given(categoryRepository.findMaxSortOrder()).willReturn(Optional.of(0));
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        AdminCategoryResponseDto result = adminCategoryService.createCategory(requestDto);

        // then
        assertThat(result.getName()).isEqualTo("운동");
        assertThat(result.getSortOrder()).isEqualTo(1);
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 순서 변경 성공")
    void updateCategoryOrders_success() {
        // given
        Category cat1 = Category.builder().id(1L).sortOrder(0).build();
        Category cat2 = Category.builder().id(2L).sortOrder(1).build();
        CategoryOrderUpdateRequestDto requestDto = new CategoryOrderUpdateRequestDto(List.of(
                new CategoryOrderUpdateRequestDto.CategoryOrderDto(1L, 1),
                new CategoryOrderUpdateRequestDto.CategoryOrderDto(2L, 0)
        ));

        given(categoryRepository.findAllById(any())).willReturn(List.of(cat1, cat2));

        // when
        adminCategoryService.updateCategoryOrders(requestDto);

        // then
        assertThat(cat1.getSortOrder()).isEqualTo(1);
        assertThat(cat2.getSortOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("카테고리 삭제(Soft Delete) 성공")
    void deleteCategory_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().id(categoryId).name("운동").build();
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        adminCategoryService.deleteCategory(categoryId);

        // then
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    @DisplayName("이미 삭제된 카테고리 삭제 시도 시 예외 발생")
    void deleteCategory_fail_alreadyDeleted() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().id(categoryId).name("운동")
                .deletedAt(java.time.LocalDateTime.now()).build();
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when & then
        assertThatThrownBy(() -> adminCategoryService.deleteCategory(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_DELETED_CATEGORY.getMessage());
    }
}

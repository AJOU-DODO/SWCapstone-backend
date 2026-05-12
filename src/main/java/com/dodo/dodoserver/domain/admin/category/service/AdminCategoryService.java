package com.dodo.dodoserver.domain.admin.category.service;

import com.dodo.dodoserver.domain.admin.category.dao.CategoryAdminRepository;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryRequestDto;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;
import com.dodo.dodoserver.domain.admin.category.dto.CategoryOrderUpdateRequestDto;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryAdminRepository categoryAdminRepository;

    @Transactional(readOnly = true)
    public List<AdminCategoryResponseDto> getCategories(boolean includeDeleted, String sortBy) {
        return categoryAdminRepository.findAllAdminCategories(includeDeleted, sortBy);
    }

    @Transactional
    public AdminCategoryResponseDto createCategory(AdminCategoryRequestDto requestDto) {
        if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        // 가장 마지막 순서 찾기 (최적화: DB에서 직접 MAX 조회)
        Integer maxSortOrder = categoryRepository.findMaxSortOrder().orElse(-1);

        Category category = Category.builder()
                .name(requestDto.getName())
                .sortOrder(maxSortOrder + 1)
                .build();

        Category savedCategory = categoryRepository.save(category);
        
        // 새로 생성된 카테고리 정보 반환 (nestCount는 0)
        return AdminCategoryResponseDto.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .sortOrder(savedCategory.getSortOrder())
                .createdAt(savedCategory.getCreatedAt())
                .deletedAt(savedCategory.getDeletedAt())
                .nestCount(0L)
                .build();
    }

    @Transactional
    public void updateCategory(Long id, AdminCategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.findByName(requestDto.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
                    }
                });

        category.setName(requestDto.getName());
    }

    @Transactional
    public void updateCategoryOrders(CategoryOrderUpdateRequestDto requestDto) {
        List<Long> categoryIds = requestDto.getOrders().stream()
                .map(CategoryOrderUpdateRequestDto.CategoryOrderDto::getId)
                .collect(Collectors.toList());

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        
        if (categories.size() != categoryIds.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        requestDto.getOrders().forEach(orderDto -> {
            Category category = categoryMap.get(orderDto.getId());
            category.setSortOrder(orderDto.getSortOrder());
        });
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CATEGORY);
        }

        categoryRepository.delete(category);
    }
}

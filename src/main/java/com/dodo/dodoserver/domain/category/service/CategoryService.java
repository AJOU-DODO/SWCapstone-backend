package com.dodo.dodoserver.domain.category.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.dto.CategoryRequestDto;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        Category category = Category.builder()
                .name(requestDto.getName())
                .build();

        return CategoryResponseDto.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        category.setName(requestDto.getName());
        return CategoryResponseDto.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        categoryRepository.delete(category);
    }
}

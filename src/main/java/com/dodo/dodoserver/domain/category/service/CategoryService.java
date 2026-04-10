package com.dodo.dodoserver.domain.category.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.dto.CategoryRequestDto;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.entity.Category;
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
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다."));

        if (categoryRepository.findByName(requestDto.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다.");
        }

        category.setName(requestDto.getName());
        return CategoryResponseDto.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다."));
        
        categoryRepository.delete(category);
    }
}

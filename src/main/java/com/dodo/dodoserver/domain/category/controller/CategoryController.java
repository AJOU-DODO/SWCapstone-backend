package com.dodo.dodoserver.domain.category.controller;

import com.dodo.dodoserver.domain.category.service.CategoryService;
import com.dodo.dodoserver.domain.category.dto.CategoryRequestDto;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping // admin
    public ApiResponseDto<CategoryResponseDto> createCategory(@RequestBody @Valid CategoryRequestDto requestDto) {
        return ApiResponseDto.success(categoryService.createCategory(requestDto));
    }

    @GetMapping
    public ApiResponseDto<List<CategoryResponseDto>> getAllCategories() {
        return ApiResponseDto.success(categoryService.getAllCategories());
    }

    @PatchMapping("/{id}") // admin
    public ApiResponseDto<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequestDto requestDto) {
        return ApiResponseDto.success(categoryService.updateCategory(id, requestDto));
    }

    @DeleteMapping("/{id}") // admin
    public ApiResponseDto<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponseDto.success("카테고리가 성공적으로 삭제되었습니다.");
    }
}

package com.dodo.dodoserver.domain.category.controller;

import com.dodo.dodoserver.domain.category.service.CategoryService;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponseDto<List<CategoryResponseDto>> getAllCategories() {
        return ApiResponseDto.success(categoryService.getAllCategories());
    }
}

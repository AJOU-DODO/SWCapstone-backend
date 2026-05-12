package com.dodo.dodoserver.domain.admin.category.controller;

import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryRequestDto;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;
import com.dodo.dodoserver.domain.admin.category.dto.CategoryOrderUpdateRequestDto;
import com.dodo.dodoserver.domain.admin.category.service.AdminCategoryService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ApiResponseDto<List<AdminCategoryResponseDto>> getCategories(
            @RequestParam(defaultValue = "true") boolean includeDeleted,
            @RequestParam(required = false) String sortBy) {
        return ApiResponseDto.success(adminCategoryService.getCategories(includeDeleted, sortBy));
    }

    @PostMapping
    public ApiResponseDto<AdminCategoryResponseDto> createCategory(
            @RequestBody @Valid AdminCategoryRequestDto requestDto) {
        return ApiResponseDto.success(adminCategoryService.createCategory(requestDto));
    }

    @PatchMapping("/{id}")
    public ApiResponseDto<Void> updateCategory(
            @PathVariable Long id,
            @RequestBody @Valid AdminCategoryRequestDto requestDto) {
        adminCategoryService.updateCategory(id, requestDto);
        return ApiResponseDto.success(null);
    }

    @PutMapping("/orders")
    public ApiResponseDto<Void> updateCategoryOrders(
            @RequestBody @Valid CategoryOrderUpdateRequestDto requestDto) {
        adminCategoryService.updateCategoryOrders(requestDto);
        return ApiResponseDto.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return ApiResponseDto.success(null);
    }
}

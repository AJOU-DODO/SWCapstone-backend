package com.dodo.dodoserver.domain.admin.category.dao;

import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;

import java.util.List;

public interface CategoryAdminRepository {
    List<AdminCategoryResponseDto> findAllAdminCategories(boolean includeDeleted, String sortBy);
}

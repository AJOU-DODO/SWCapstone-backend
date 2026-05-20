package com.dodo.dodoserver.domain.admin.nest.dao;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminNestRepositoryCustom {
    Page<AdminNestResponseDto> findNestsForAdmin(
            Pageable pageable, 
            LocalDate startDate, 
            LocalDate endDate, 
            String sort, 
            String includeDeleted);
}

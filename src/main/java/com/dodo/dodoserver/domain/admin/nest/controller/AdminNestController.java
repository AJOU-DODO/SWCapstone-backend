package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDetailResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/nests")
@RequiredArgsConstructor
public class AdminNestController {

    private final AdminNestService adminNestService;

    @GetMapping
    public ApiResponseDto<Page<AdminNestResponseDto>> getNests(
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "ACTIVE_ONLY") String includeDeleted) {
        return ApiResponseDto.success(adminNestService.getNests(pageable, startDate, endDate, sort, includeDeleted));
    }

    @GetMapping("/{nestId}")
    public ApiResponseDto<AdminNestDetailResponseDto> getNestDetail(@PathVariable Long nestId) {
        return ApiResponseDto.success(adminNestService.getNestDetail(nestId));
    }

    @GetMapping("/{nestId}/comments")
    public ApiResponseDto<List<AdminCommentResponseDto>> getNestComments(@PathVariable Long nestId) {
        return ApiResponseDto.success(adminNestService.getNestComments(nestId));
    }
}

package com.dodo.dodoserver.domain.admin.postcard.controller;

import com.dodo.dodoserver.domain.admin.postcard.service.AdminPostcardService;
import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Postcard", description = "관리자용 엽서 관리 API")
@RestController
@RequestMapping("/api/v1/admin/postcards")
@RequiredArgsConstructor
public class AdminPostcardController {

    private final AdminPostcardService adminPostcardService;

    @Operation(summary = "신고된 엽서 목록 조회", description = "신고가 접수된 엽서 목록을 집계하여 조회합니다. PENDING 또는 PROCESSED 상태인 데이터만 노출됩니다.")
    @GetMapping("/reported")
    public ApiResponseDto<Page<AdminPostcardReportResponseDto>> getReportedPostcards(
            Pageable pageable,
            @RequestParam(required = false) String sort) {
        return ApiResponseDto.success(adminPostcardService.getReportedPostcards(pageable, sort));
    }
}

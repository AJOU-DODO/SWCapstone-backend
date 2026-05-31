package com.dodo.dodoserver.domain.admin.inquiry.controller;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryDetailResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.InquiryAnswerRequestDto;
import com.dodo.dodoserver.domain.admin.inquiry.service.AdminInquiryService;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Inquiry API", description = "관리자 1:1 문의 관리 API")
@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "문의 리스트 조회 (필터링 포함)")
    @GetMapping
    public ApiResponseDto<Page<AdminInquiryResponseDto>> getInquiries(
            @RequestParam(required = false) InquiryStatus status,
            Pageable pageable) {
        return ApiResponseDto.success(adminInquiryService.getInquiries(status, pageable));
    }

    @Operation(summary = "문의 상세 조회")
    @GetMapping("/{inquiryId}")
    public ApiResponseDto<AdminInquiryDetailResponseDto> getInquiryDetail(@PathVariable Long inquiryId) {
        return ApiResponseDto.success(adminInquiryService.getInquiryDetail(inquiryId));
    }

    @Operation(summary = "문의 답변 등록")
    @PostMapping("/{inquiryId}/answer")
    public ApiResponseDto<Void> answerInquiry(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequestDto requestDto) {
        adminInquiryService.answerInquiry(inquiryId, requestDto);
        return ApiResponseDto.success(null);
    }
}

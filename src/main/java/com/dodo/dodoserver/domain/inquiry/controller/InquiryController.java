package com.dodo.dodoserver.domain.inquiry.controller;

import com.dodo.dodoserver.domain.inquiry.dto.InquiryRequestDto;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.service.InquiryService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry API", description = "사용자 1:1 문의 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록")
    @PostMapping
    public ApiResponseDto<InquiryResponseDto> createInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody InquiryRequestDto requestDto) {
        return ApiResponseDto.success(inquiryService.createInquiry(userPrincipal.getId(), requestDto));
    }

    @Operation(summary = "내 문의 리스트 조회")
    @GetMapping("/me")
    public ApiResponseDto<Page<InquiryResponseDto>> getMyInquiries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        return ApiResponseDto.success(inquiryService.getMyInquiries(userPrincipal.getId(), pageable));
    }

    @Operation(summary = "문의 수정")
    @PutMapping("/{inquiryId}")
    public ApiResponseDto<InquiryResponseDto> updateInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryRequestDto requestDto) {
        return ApiResponseDto.success(inquiryService.updateInquiry(userPrincipal.getId(), inquiryId, requestDto));
    }

    @Operation(summary = "문의 삭제")
    @DeleteMapping("/{inquiryId}")
    public ApiResponseDto<Void> deleteInquiry(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(userPrincipal.getId(), inquiryId);
        return ApiResponseDto.success(null);
    }
}

package com.dodo.dodoserver.domain.admin.notice.controller;

import com.dodo.dodoserver.domain.admin.notice.dto.NoticeRequestDto;
import com.dodo.dodoserver.domain.admin.notice.service.AdminNoticeService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Notice API", description = "관리자 공지사항 관리 API")
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @Operation(summary = "공지사항 등록 (초안)")
    @PostMapping
    public ApiResponseDto<Long> createNotice(@RequestBody @Valid NoticeRequestDto requestDto) {
        return ApiResponseDto.success(adminNoticeService.createNotice(requestDto));
    }

    @Operation(summary = "공지사항 수정")
    @PutMapping("/{noticeId}")
    public ApiResponseDto<Void> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeRequestDto requestDto) {
        adminNoticeService.updateNotice(noticeId, requestDto);
        return ApiResponseDto.success(null);
    }

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{noticeId}")
    public ApiResponseDto<Void> deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
        return ApiResponseDto.success(null);
    }

    @Operation(summary = "공지사항 발행 (FCM 발송)")
    @PostMapping("/{noticeId}/publish")
    public ApiResponseDto<Void> publishNotice(@PathVariable Long noticeId) {
        adminNoticeService.publishNotice(noticeId);
        return ApiResponseDto.success(null);
    }

    @Operation(summary = "관리자 공지사항 목록 조회")
    @GetMapping
    public ApiResponseDto<Page<NoticeResponseDto>> getAllNotices(Pageable pageable) {
        return ApiResponseDto.success(adminNoticeService.getAllNotices(pageable));
    }

    @Operation(summary = "관리자 공지사항 상세 조회")
    @GetMapping("/{noticeId}")
    public ApiResponseDto<NoticeResponseDto> getNoticeDetail(@PathVariable Long noticeId) {
        return ApiResponseDto.success(adminNoticeService.getNoticeDetail(noticeId));
    }
}

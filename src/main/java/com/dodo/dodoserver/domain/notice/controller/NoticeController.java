package com.dodo.dodoserver.domain.notice.controller;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import com.dodo.dodoserver.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notice API", description = "사용자 공지사항 API")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회 (발행된 항목만)")
    @GetMapping
    public ApiResponseDto<Page<NoticeResponseDto>> getPublishedNotices(Pageable pageable) {
        return ApiResponseDto.success(noticeService.getPublishedNotices(pageable));
    }

    @Operation(summary = "공지사항 상세 조회 (발행된 항목만)")
    @GetMapping("/{noticeId}")
    public ApiResponseDto<NoticeResponseDto> getNoticeDetail(@PathVariable Long noticeId) {
        return ApiResponseDto.success(noticeService.getNoticeDetail(noticeId));
    }
}

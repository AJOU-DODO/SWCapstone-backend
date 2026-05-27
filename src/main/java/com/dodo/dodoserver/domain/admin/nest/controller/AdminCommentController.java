package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Comment", description = "관리자용 댓글 관리 API")
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminNestService adminNestService;

    @Operation(summary = "관리자 전용 댓글 삭제", description = "댓글을 소프트 삭제 처리하고 작성자에게 사유와 함께 알림을 발송합니다.")
    @DeleteMapping("/{commentId}")
    public ApiResponseDto<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestBody(required = false) AdminCommentDeleteRequestDto requestDto) {
        adminNestService.deleteCommentForAdmin(commentId, requestDto != null ? requestDto : new AdminCommentDeleteRequestDto());
        return ApiResponseDto.success(null);
    }
}

package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Comment", description = "관리자용 댓글 관리 API")
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminNestService adminNestService;

    @Operation(summary = "관리자 전용 댓글 삭제", description = "댓글을 소프트 삭제 처리합니다. 대댓글 구조를 유지하기 위해 논리 삭제만 수행합니다.")
    @DeleteMapping("/{commentId}")
    public ApiResponseDto<Void> deleteComment(@PathVariable Long commentId) {
        adminNestService.deleteCommentForAdmin(commentId);
        return ApiResponseDto.success(null);
    }
}

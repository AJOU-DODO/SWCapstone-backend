package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminNestService adminNestService;

    @DeleteMapping("/{commentId}")
    public ApiResponseDto<Void> deleteComment(@PathVariable Long commentId) {
        adminNestService.deleteComment(commentId);
        return ApiResponseDto.success(null);
    }
}

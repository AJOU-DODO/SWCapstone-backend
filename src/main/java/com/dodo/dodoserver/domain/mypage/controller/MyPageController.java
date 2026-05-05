package com.dodo.dodoserver.domain.mypage.controller;

import com.dodo.dodoserver.domain.mypage.dto.*;
import com.dodo.dodoserver.domain.mypage.service.MyPageService;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/nests")
    public ApiResponseDto<Page<MyPageNestResponseDto>> getMyNests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyNests(userPrincipal.getId(), categoryId, pageable));
    }

    @GetMapping("/comments")
    public ApiResponseDto<Page<MyPageCommentResponseDto>> getMyComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyComments(userPrincipal.getId(), pageable));
    }

    @GetMapping("/drafts")
    public ApiResponseDto<Page<MyPageDraftResponseDto>> getMyDrafts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyDrafts(userPrincipal.getId(), pageable));
    }

    @GetMapping("/statistics")
    public ApiResponseDto<MyPageStatisticsResponseDto> getMyStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(myPageService.getMyStatistics(userPrincipal.getId()));
    }

    @GetMapping("/unlocks")
    public ApiResponseDto<Page<MyPageNestResponseDto>> getMyUnlockedNests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyUnlockedNests(userPrincipal.getId(), pageable));
    }

    @GetMapping("/reactions")
    public ApiResponseDto<Page<MyPageCommentResponseDto>> getCommentsOnMyNests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getCommentsOnMyNests(userPrincipal.getId(), pageable));
    }

    @GetMapping("/likes")
    public ApiResponseDto<Page<MyPageNestResponseDto>> getMyLikedNests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyLikedNests(userPrincipal.getId(), pageable));
    }

    @GetMapping("/postcards")
    public ApiResponseDto<Page<MyPagePostcardResponseDto>> getMyPostcards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false, defaultValue = "ALL") String filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponseDto.success(myPageService.getMyPostcards(userPrincipal.getId(), filter, pageable));
    }
}

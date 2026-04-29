package com.dodo.dodoserver.domain.postcard.controller;

import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.postcard.dto.*;
import com.dodo.dodoserver.domain.postcard.service.PostcardService;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostcardController {

    private final PostcardService postcardService;

    /**
     * 엽서 등록 (생성)
     */
    @PostMapping("/postcards")
    public ApiResponseDto<PostcardResponseDto> createPostcard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PostcardCreateRequestDto requestDto) {
        return ApiResponseDto.success(postcardService.createPostcard(userPrincipal.getId(), requestDto));
    }

    /**
     * 엽서 상세 조회
     */
    @GetMapping("/postcards/{id}")
    public ApiResponseDto<PostcardResponseDto> getPostcardDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ApiResponseDto.success(postcardService.getPostcardDetail(userPrincipal.getId(), id));
    }

    /**
     * 엽서 수정
     */
    @PutMapping("/postcards/{id}")
    public ApiResponseDto<PostcardResponseDto> updatePostcard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody PostcardCreateRequestDto requestDto) {
        return ApiResponseDto.success(postcardService.updatePostcard(userPrincipal.getId(), id, requestDto));
    }

    /**
     * 엽서 삭제
     */
    @DeleteMapping("/postcards/{id}")
    public ApiResponseDto<Void> deletePostcard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        postcardService.deletePostcard(userPrincipal.getId(), id);
        return ApiResponseDto.success(null);
    }

    /**
     * 엽서 교환 가능 여부 확인
     */
    @GetMapping("/postcards/exchange-check")
    public ApiResponseDto<PostcardExchangeCheckResponseDto> checkExchangeAvailability(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(postcardService.checkExchangeAvailability(userPrincipal.getId()));
    }

    /**
     * 엽서 교환
     */
    @PostMapping("/nests/{id}/exchange")
    public ApiResponseDto<PostcardResponseDto> exchangePostcard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "id") Long nestId,
            @RequestBody PostcardExchangeRequestDto requestDto) {
        return ApiResponseDto.success(postcardService.exchangePostcard(userPrincipal.getId(), nestId, requestDto));
    }

    /**
     * 엽서 이모지 반응
     */
    @PostMapping("/postcards/{id}/reactions")
    public ApiResponseDto<Void> addReaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam ReactionType type) {
        postcardService.addReaction(userPrincipal.getId(), id, type);
        return ApiResponseDto.success(null);
    }
}

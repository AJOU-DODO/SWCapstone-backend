package com.dodo.dodoserver.domain.nest.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dodo.dodoserver.domain.nest.dto.CommentCreateRequestDto;
import com.dodo.dodoserver.domain.nest.dto.CommentUpdateRequestDto;
import com.dodo.dodoserver.domain.nest.dto.NestCreateRequestDto;
import com.dodo.dodoserver.domain.nest.dto.NestDetailResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestSummaryResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestUpdateRequestDto;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.global.common.ApiResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/nests")
@RequiredArgsConstructor
public class NestPostController {

	private final NestService nestService;

	/**
	 * 새로운 둥지(Nest) 생성
	 */
	@PostMapping
	public ApiResponseDto<NestSummaryResponseDto> createNest(
		Authentication authentication,
		@RequestBody @Valid NestCreateRequestDto requestDto) {

		String email = authentication.getName();
		return ApiResponseDto.success(nestService.createNest(email, requestDto));
	}

	/**
	 * 둥지 상세 정보 조회
	 */
	@GetMapping("/{id}")
	public ApiResponseDto<NestDetailResponseDto> getNestDetail(
		Authentication authentication,
		@PathVariable Long id) {

		String email = authentication.getName();
		return ApiResponseDto.success(nestService.getNestDetail(email, id));
	}

	/**
	 * 둥지 정보 수정 (작성자 전용)
	 */
	@PatchMapping("/{id}")
	public ApiResponseDto<NestSummaryResponseDto> updateNest(
		Authentication authentication,
		@PathVariable Long id,
		@RequestBody @Valid NestUpdateRequestDto requestDto) {

		String email = authentication.getName();
		return ApiResponseDto.success(nestService.updateNest(email, id, requestDto));
	}

	/**
	 * 둥지 삭제 (작성자 전용)
	 */
	@DeleteMapping("/{id}")
	public ApiResponseDto<String> deleteNest(
		Authentication authentication,
		@PathVariable Long id) {

		String email = authentication.getName();
		nestService.deleteNest(email, id);
		return ApiResponseDto.success("둥지가 성공적으로 삭제되었습니다.");
	}

	/**
	 * 둥지 댓글 작성
	 */
	@PostMapping("/{id}/comments")
	public ApiResponseDto<String> createComment(
		Authentication authentication,
		@PathVariable Long id,
		@RequestBody @Valid CommentCreateRequestDto requestDto) {

		String email = authentication.getName();
		nestService.createComment(email, id, requestDto);
		return ApiResponseDto.success("댓글이 성공적으로 작성되었습니다.");
	}

	/**
	 * 댓글 수정
	 */
	@PatchMapping("/comments/{commentId}")
	public ApiResponseDto<String> updateComment(
		Authentication authentication,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentUpdateRequestDto requestDto) {

		String email = authentication.getName();
		nestService.updateComment(email, commentId, requestDto);
		return ApiResponseDto.success("댓글이 성공적으로 수정되었습니다.");
	}

	/**
	 * 댓글 삭제
	 */
	@DeleteMapping("/comments/{commentId}")
	public ApiResponseDto<String> deleteComment(
		Authentication authentication,
		@PathVariable Long commentId) {

		String email = authentication.getName();
		nestService.deleteComment(email, commentId);
		return ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.");
	}



	/**
	 * 둥지 리액션(좋아요/싫어요) 등록 및 수정
	 */
	@PostMapping("/{id}/reaction")
	public ApiResponseDto<String> handleReaction(
		Authentication authentication,
		@PathVariable Long id,
		@RequestParam ReactionType type) {

		String email = authentication.getName();
		nestService.handleReaction(email, id, type);
		return ApiResponseDto.success("리액션이 성공적으로 처리되었습니다.");
	}
}

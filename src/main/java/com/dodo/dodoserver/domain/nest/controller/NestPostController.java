package com.dodo.dodoserver.domain.nest.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dodo.dodoserver.global.security.UserPrincipal;
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
import com.dodo.dodoserver.domain.nest.dto.CommentResponseDto;
import com.dodo.dodoserver.domain.nest.dto.CommentUpdateRequestDto;
import com.dodo.dodoserver.domain.nest.dto.NestCreateRequestDto;
import com.dodo.dodoserver.domain.nest.dto.NestDetailResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestSimpleResponseDto;
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
	public ApiResponseDto<NestSimpleResponseDto> createNest(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestBody @Valid NestCreateRequestDto requestDto) {

		return ApiResponseDto.success(nestService.createNest(principal.getId(), requestDto));
	}

	/**
	 * 둥지 상세 정보 조회
	 */
	@GetMapping("/{id}")
	public ApiResponseDto<NestDetailResponseDto> getNestDetail(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id) {

		return ApiResponseDto.success(nestService.getNestDetail(principal.getId(), id));
	}

	/**
	 * 둥지 정보 수정 (작성자 전용)
	 */
	@PatchMapping("/{id}")
	public ApiResponseDto<NestSimpleResponseDto> updateNest(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id,
		@RequestBody @Valid NestUpdateRequestDto requestDto) {

		return ApiResponseDto.success(nestService.updateNest(principal.getId(), id, requestDto));
	}

	/**
	 * 둥지 삭제 (작성자 전용)
	 */
	@DeleteMapping("/{id}")
	public ApiResponseDto<String> deleteNest(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id) {

		nestService.deleteNest(principal.getId(), id);
		return ApiResponseDto.success("둥지가 성공적으로 삭제되었습니다.");
	}

	/**
	 * 둥지 댓글 작성
	 */
	@PostMapping("/{id}/comments")
	public ApiResponseDto<String> createComment(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id,
		@RequestBody @Valid CommentCreateRequestDto requestDto) {

		nestService.createComment(principal.getId(), id, requestDto);
		return ApiResponseDto.success("댓글이 성공적으로 작성되었습니다.");
	}

	/**
	 * 둥지 댓글 리스트 조회
	 */
	@GetMapping("/{id}/comments")
	public ApiResponseDto<List<CommentResponseDto>> getComments(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id,
		@RequestParam(required = false, defaultValue = "DEFAULT") String sortBy) {
		
		Long userId = (principal != null) ? principal.getId() : null;
		return ApiResponseDto.success(nestService.getCommentsByNestId(userId, id, sortBy));
	}

	/**
	 * 댓글 좋아요 처리 (Toggle)
	 */
	@PostMapping("/comments/{commentId}/like")
	public ApiResponseDto<String> handleCommentLike(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long commentId) {

		nestService.handleCommentLike(principal.getId(), commentId);
		return ApiResponseDto.success("댓글 좋아요 처리가 완료되었습니다.");
	}

	/**
	 * 댓글 수정
	 */
	@PatchMapping("/comments/{commentId}")
	public ApiResponseDto<String> updateComment(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentUpdateRequestDto requestDto) {

		nestService.updateComment(principal.getId(), commentId, requestDto);
		return ApiResponseDto.success("댓글이 성공적으로 수정되었습니다.");
	}

	/**
	 * 댓글 삭제
	 */
	@DeleteMapping("/comments/{commentId}")
	public ApiResponseDto<String> deleteComment(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long commentId) {

		nestService.deleteComment(principal.getId(), commentId);
		return ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다.");
	}



	/**
	 * 둥지 리액션(좋아요/싫어요) 등록 및 수정
	 */
	@PostMapping("/{id}/reaction")
	public ApiResponseDto<String> handleReaction(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable Long id,
		@RequestParam ReactionType type) {

		nestService.handleReaction(principal.getId(), id, type);
		return ApiResponseDto.success("리액션이 성공적으로 처리되었습니다.");
	}
}

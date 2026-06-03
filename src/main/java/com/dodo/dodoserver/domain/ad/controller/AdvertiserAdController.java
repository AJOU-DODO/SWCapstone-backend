package com.dodo.dodoserver.domain.ad.controller;

import com.dodo.dodoserver.domain.ad.dto.AdProposalRequestDto;
import com.dodo.dodoserver.domain.ad.dto.AdProposalResponseDto;
import com.dodo.dodoserver.domain.ad.dto.AdStatisticsResponseDto;
import com.dodo.dodoserver.domain.ad.dto.AdvertiserMyAccountResponseDto;
import com.dodo.dodoserver.domain.ad.service.AdvertiserAdService;
import com.dodo.dodoserver.domain.nest.dto.NestSimpleResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Advertiser Ad", description = "광고주 전용 광고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/advertiser/ads")
public class AdvertiserAdController {

    private final AdvertiserAdService advertiserAdService;

    /**
     * 내 광고주 계정 정보 조회 (잔여 개수, 만료일 등)
     */
    @Operation(summary = "내 광고주 계정 정보 조회", description = "광고주의 발행 가능 광고 개수, 현재 게시 중인 광고 수, 권한 만료일 등을 조회합니다.")
    @GetMapping("/account")
    public ApiResponseDto<AdvertiserMyAccountResponseDto> getMyAccountInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(advertiserAdService.getMyAccountInfo(userPrincipal.getId()));
    }

    /**
     * 광고 신청
     */
    @Operation(summary = "광고 신청", description = "새로운 광고 게시물을 신청(PENDING 상태)합니다. 광고주 권한 및 잔여 개수를 검증합니다.")
    @PostMapping("/proposals")
    public ApiResponseDto<Void> createProposal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AdProposalRequestDto requestDto) {
        advertiserAdService.createProposal(userPrincipal.getId(), requestDto);
        return ApiResponseDto.success(null);
    }

    /**
     * 광고 신청 수정 (재심사 요청)
     */
    @Operation(summary = "광고 신청 수정", description = "반려되었거나 대기 중인 광고 신청의 내용을 수정하고 재심사를 요청합니다.")
    @PutMapping("/proposals/{proposalId}")
    public ApiResponseDto<Void> updateProposal(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long proposalId,
            @Valid @RequestBody AdProposalRequestDto requestDto) {
        advertiserAdService.updateProposal(userPrincipal.getId(), proposalId, requestDto);
        return ApiResponseDto.success(null);
    }

    /**
     * 내 광고 신청 내역 조회
     */
    @Operation(summary = "내 광고 신청 내역 조회", description = "광고주 본인이 신청한 모든 광고 신청 내역(대기, 승인, 반려)을 조회합니다.")
    @GetMapping("/proposals/me")
    public ApiResponseDto<List<AdProposalResponseDto>> getMyProposals(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(advertiserAdService.getMyProposals(userPrincipal.getId()));
    }

    /**
     * 발행된 내 광고 둥지 목록 조회
     */
    @Operation(summary = "발행된 내 광고 둥지 목록 조회", description = "광고주 본인이 발행하여 현재 게시 중인 광고 둥지 목록을 조회합니다.")
    @GetMapping("/nests/me")
    public ApiResponseDto<List<NestSimpleResponseDto>> getMyAdNests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ApiResponseDto.success(advertiserAdService.getMyAdNests(userPrincipal.getId()));
    }

    /**
     * 광고 성과 통계 조회
     */
    @Operation(summary = "광고 성과 통계 조회", description = "본인이 발행한 특정 광고 둥지의 누적 노출수(impressions)와 클릭수(clicks)를 조회합니다.")
    @GetMapping("/nests/{nestId}/statistics")
    public ApiResponseDto<AdStatisticsResponseDto> getAdStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long nestId) {
        return ApiResponseDto.success(advertiserAdService.getAdStatistics(userPrincipal.getId(), nestId));
    }
}

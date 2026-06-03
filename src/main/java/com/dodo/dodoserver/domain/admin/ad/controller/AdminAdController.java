package com.dodo.dodoserver.domain.admin.ad.controller;

import com.dodo.dodoserver.domain.admin.ad.dto.*;
import com.dodo.dodoserver.domain.admin.ad.service.AdminAdService;
import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Ad", description = "관리자용 광고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/ads")
public class AdminAdController {

    private final AdminAdService adminAdService;

    /**
     * 광고주 권한 부여를 위한 유저 검색
     */
    @Operation(summary = "광고주 권한 부여를 위한 유저 검색", description = "이메일 키워드를 통해 광고주 권한을 부여할 유저를 검색합니다.")
    @GetMapping("/users/search")
    public ApiResponseDto<List<UserAdminResponseDto>> searchUsers(@RequestParam String email) {
        return ApiResponseDto.success(adminAdService.searchUsersByEmail(email));
    }

    /**
     * 광고주 권한 부여 및 수정
     */
    @Operation(summary = "광고주 권한 부여 및 수정", description = "유저에게 광고주(ADVERTISER) 역할을 부여하고 발행 가능 개수와 기한을 설정합니다.")
    @PostMapping("/advertisers/{userId}")
    public ApiResponseDto<Void> grantAdvertiserRole(
            @PathVariable Long userId,
            @Valid @RequestBody AdvertiserAuthorityRequestDto requestDto) {
        adminAdService.grantAdvertiserRole(userId, requestDto);
        return ApiResponseDto.success(null);
    }

    /**
     * 광고주 목록 조회
     */
    @Operation(summary = "광고주 목록 조회", description = "시스템에 등록된 광고주 목록을 페이징하여 조회합니다.")
    @GetMapping("/advertisers")
    public ApiResponseDto<Page<AdvertiserResponseDto>> getAllAdvertisers(Pageable pageable) {
        return ApiResponseDto.success(adminAdService.getAllAdvertisers(pageable));
    }

    /**
     * 광고 신청 목록 조회 (PENDING)
     */
    @Operation(summary = "광고 신청 목록 조회 (PENDING)", description = "승인 대기 중인 광고 신청 목록을 조회합니다.")
    @GetMapping("/proposals")
    public ApiResponseDto<List<AdProposalAdminResponseDto>> getPendingProposals() {
        return ApiResponseDto.success(adminAdService.getPendingProposals());
    }

    /**
     * 광고 신청 승인
     */
    @Operation(summary = "광고 신청 승인", description = "광고 신청을 승인하여 실제 광고 둥지를 생성하고 발행합니다.")
    @PostMapping("/proposals/{proposalId}/approve")
    public ApiResponseDto<Void> approveProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody AdApproveRequestDto requestDto) {
        adminAdService.approveProposal(proposalId, requestDto);
        return ApiResponseDto.success(null);
    }

    /**
     * 광고 신청 반려
     */
    @Operation(summary = "광고 신청 반려", description = "광고 신청을 반려하고 반려 사유를 기록합니다.")
    @PostMapping("/proposals/{proposalId}/reject")
    public ApiResponseDto<Void> rejectProposal(
            @PathVariable Long proposalId,
            @Valid @RequestBody AdRejectRequestDto requestDto) {
        adminAdService.rejectProposal(proposalId, requestDto);
        return ApiResponseDto.success(null);
    }

    /**
     * 게시된 광고 목록 조회
     */
    @Operation(summary = "게시된 광고 목록 조회", description = "게시 중이거나 삭제된 광고 둥지 목록과 통계를 조회합니다.")
    @GetMapping("/nests")
    public ApiResponseDto<Page<AdNestAdminResponseDto>> getAdNests(
            @RequestParam(required = false, defaultValue = "ALL") AdStatusFilter status,
            Pageable pageable) {
        return ApiResponseDto.success(adminAdService.getAdNests(status, pageable));
    }

    /**
     * 광고 강제 삭제 (Soft Delete)
     */
    @Operation(summary = "광고 강제 삭제", description = "관리자 권한으로 게시 중인 광고를 즉시 삭제(Soft Delete)합니다.")
    @DeleteMapping("/nests/{nestId}")
    public ApiResponseDto<Void> deleteAdNest(@PathVariable Long nestId) {
        adminAdService.deleteAdNest(nestId);
        return ApiResponseDto.success(null);
    }
}

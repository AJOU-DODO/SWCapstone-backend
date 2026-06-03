package com.dodo.dodoserver.domain.admin.ad.controller;

import com.dodo.dodoserver.domain.admin.ad.dto.*;
import com.dodo.dodoserver.domain.admin.ad.service.AdminAdService;
import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/ads")
public class AdminAdController {

    private final AdminAdService adminAdService;

    /**
     * 광고주 권한 부여를 위한 유저 검색
     */
    @GetMapping("/users/search")
    public ApiResponseDto<List<UserAdminResponseDto>> searchUsers(@RequestParam String email) {
        return ApiResponseDto.success(adminAdService.searchUsersByEmail(email));
    }

    /**
     * 광고주 권한 부여 및 수정
     */
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
    @GetMapping("/advertisers")
    public ApiResponseDto<Page<AdvertiserResponseDto>> getAllAdvertisers(Pageable pageable) {
        return ApiResponseDto.success(adminAdService.getAllAdvertisers(pageable));
    }

    /**
     * 광고 신청 목록 조회 (PENDING)
     */
    @GetMapping("/proposals")
    public ApiResponseDto<List<AdProposalAdminResponseDto>> getPendingProposals() {
        return ApiResponseDto.success(adminAdService.getPendingProposals());
    }

    /**
     * 광고 신청 승인
     */
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
    @GetMapping("/nests")
    public ApiResponseDto<Page<AdNestAdminResponseDto>> getAdNests(Pageable pageable) {
        return ApiResponseDto.success(adminAdService.getAdNests(pageable));
    }

    /**
     * 광고 강제 삭제 (Soft Delete)
     */
    @DeleteMapping("/nests/{nestId}")
    public ApiResponseDto<Void> deleteAdNest(@PathVariable Long nestId) {
        adminAdService.deleteAdNest(nestId);
        return ApiResponseDto.success(null);
    }
}

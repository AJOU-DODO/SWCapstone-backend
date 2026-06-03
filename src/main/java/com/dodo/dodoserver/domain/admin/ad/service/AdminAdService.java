package com.dodo.dodoserver.domain.admin.ad.service;

import com.dodo.dodoserver.domain.ad.dao.AdProposalRepository;
import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.admin.ad.dao.AdAdminRepository;
import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.admin.ad.dto.*;
import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.nest.dao.NestCategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.NestCommentRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestCategory;
import com.dodo.dodoserver.domain.nest.entity.NestImage;
import com.dodo.dodoserver.domain.nest.entity.NestLocation;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAdService {

    private final UserRepository userRepository;
    private final AdvertiserAuthorityRepository advertiserAuthorityRepository;
    private final AdProposalRepository adProposalRepository;
    private final NestRepository nestRepository;
    private final NestAdInfoRepository nestAdInfoRepository;
    private final AdAdminRepository adAdminRepository;
    private final CategoryRepository categoryRepository;
    private final NestCategoryRepository nestCategoryRepository;
    private final NestCommentRepository nestCommentRepository;

    /**
     * 이메일로 유저 검색
     */
    @Transactional(readOnly = true)
    public List<UserAdminResponseDto> searchUsersByEmail(String email) {
        return userRepository.findAllByEmailContaining(email).stream()
                .map(user -> UserAdminResponseDto.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .nestCount(nestRepository.countByCreator(user))
                        .commentCount(nestCommentRepository.countByUser(user))
                        .sanctionedUntil(user.getSanctionedUntil())
                        .isSanctioned(user.getSanctionedUntil() != null && user.getSanctionedUntil().isAfter(LocalDateTime.now()))
                        .build())
                .toList();
    }

    /**
     * 광고주 권한 부여/수정
     */
    @Transactional
    public void grantAdvertiserRole(Long userId, AdvertiserAuthorityRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setRole(Role.ADVERTISER);

        AdvertiserAuthority authority = advertiserAuthorityRepository.findByUser(user)
                .orElse(AdvertiserAuthority.builder().user(user).build());

        authority.setAllowedAdCount(requestDto.getAllowedAdCount());
        authority.setExpiredAt(requestDto.getExpiredAt());

        advertiserAuthorityRepository.save(authority);
        log.info("광고주 권한 부여 완료: User={}, AllowedAdCount={}, ExpiredAt={}", userId, requestDto.getAllowedAdCount(), requestDto.getExpiredAt());
    }

    /**
     * 광고주 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<AdvertiserResponseDto> getAllAdvertisers(Pageable pageable) {
        return advertiserAuthorityRepository.findAll(pageable)
                .map(AdvertiserResponseDto::from);
    }

    /**
     * 광고 신청 목록 조회 (PENDING)
     */
    @Transactional(readOnly = true)
    public List<AdProposalAdminResponseDto> getPendingProposals() {
        List<AdProposal> proposals = adProposalRepository.findAllByStatus(AdProposalStatus.PENDING);

        // Extract all unique category IDs
        List<Long> allCategoryIds = proposals.stream()
                .map(AdProposal::getCategoryIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .distinct()
                .toList();

        // Fetch category names and create a map
        Map<Long, String> categoryMap = categoryRepository.findAllById(allCategoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return proposals.stream()
                .map(proposal -> {
                    List<Long> categoryIds = proposal.getCategoryIds();
                    List<String> categoryNames = categoryIds != null ? categoryIds.stream()
                            .map(categoryMap::get)
                            .filter(Objects::nonNull)
                            .toList() : List.of();
                    return AdProposalAdminResponseDto.from(proposal, categoryNames);
                })
                .toList();
    }

    /**
     * 광고 신청 승인
     */
    @Transactional
    public void approveProposal(Long proposalId, AdApproveRequestDto requestDto) {
        AdProposal proposal = adProposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (proposal.getStatus() != AdProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User advertiser = proposal.getAdvertiser();
        if (advertiser.getRole() != Role.ADVERTISER) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        AdvertiserAuthority authority = advertiserAuthorityRepository.findByUser(advertiser)
                .orElseThrow(() -> new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED));

        // 권한 만료 체크
        if (authority.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ADVERTISER_AUTHORITY_EXPIRED);
        }

        // 발행 가능 개수 체크 (현재 승인된 광고 수)
        long currentAdCount = nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(advertiser);
        if (currentAdCount >= authority.getAllowedAdCount()) {
            throw new BusinessException(ErrorCode.AD_COUNT_LIMIT_EXCEEDED);
        }

        // 1. Nest 생성
        Nest nest = Nest.builder()
                .creator(proposal.getAdvertiser())
                .title(proposal.getTitle())
                .content(proposal.getContent())
                .unlockRadius(proposal.getUnlockRadius())
                .isAd(true)
                .build();

        // 2. NestLocation 생성
        NestLocation location = NestLocation.builder()
                .nest(nest)
                .point(proposal.getPoint())
                .build();
        nest.setLocation(location);

        // 3. NestImage 생성
        if (proposal.getImageUrls() != null) {
            List<String> urls = proposal.getImageUrls();
            IntStream.range(0, urls.size()).forEach(i -> {
                NestImage image = NestImage.builder()
                        .nest(nest)
                        .imageUrl(urls.get(i))
                        .sortOrder(i + 1)
                        .build();
                nest.addImage(image);
            });
        }

        Nest savedNest = nestRepository.save(nest);

        // 4. NestCategory 생성
        if (proposal.getCategoryIds() != null) {
            proposal.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

                NestCategory nestCategory = NestCategory.builder()
                        .nest(savedNest)
                        .category(category)
                        .build();
                nestCategoryRepository.save(nestCategory);
            });
        }

        // 5. NestAdInfo 생성
        NestAdInfo adInfo = NestAdInfo.builder()
                .nest(savedNest)
                .expiredAt(requestDto.getExpiredAt())
                .priorityScore(requestDto.getPriorityScore())
                .build();
        nestAdInfoRepository.save(adInfo);

        // 6. 신청 삭제
        adProposalRepository.delete(proposal);

        log.info("광고 신청 승인 완료: ProposalId={}, NestId={}", proposalId, savedNest.getId());
    }

    /**
     * 광고 신청 반려
     */
    @Transactional
    public void rejectProposal(Long proposalId, AdRejectRequestDto requestDto) {
        AdProposal proposal = adProposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (proposal.getStatus() != AdProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        proposal.setStatus(AdProposalStatus.REJECTED);
        proposal.setRejectReason(requestDto.getRejectReason());

        log.info("광고 신청 반려 완료: ProposalId={}, Reason={}", proposalId, requestDto.getRejectReason());
    }

    /**
     * 게시된 광고 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<AdNestAdminResponseDto> getAdNests(AdStatusFilter status, Pageable pageable) {
        return adAdminRepository.findAllWithFilter(status, pageable)
                .map(adInfo -> AdNestAdminResponseDto.of(
                        adInfo.getNest(),
                        adInfo.getExpiredAt(),
                        adInfo.getPriorityScore(),
                        adInfo.getImpressions(),
                        adInfo.getClicks()
                ));
    }

    /**
     * 광고 강제 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteAdNest(Long nestId) {
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.isAd()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // Nest 본체 Soft Delete (NestAdInfo는 통계 보존을 위해 유지)
        nest.setDeletedAt(LocalDateTime.now());
        log.info("광고 강제 삭제 완료: NestId={}", nestId);
    }
}

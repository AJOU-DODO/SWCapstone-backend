package com.dodo.dodoserver.domain.ad.service;

import com.dodo.dodoserver.domain.ad.dao.AdProposalRepository;
import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.ad.dto.AdProposalRequestDto;
import com.dodo.dodoserver.domain.ad.dto.AdProposalResponseDto;
import com.dodo.dodoserver.domain.ad.dto.AdStatisticsResponseDto;
import com.dodo.dodoserver.domain.ad.dto.AdvertiserMyAccountResponseDto;
import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.dto.NestSimpleResponseDto;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertiserAdService {

    private final UserRepository userRepository;
    private final AdvertiserAuthorityRepository advertiserAuthorityRepository;
    private final AdProposalRepository adProposalRepository;
    private final NestAdInfoRepository nestAdInfoRepository;
    private final NestRepository nestRepository;
    private final CategoryRepository categoryRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 내 광고주 계정 정보 조회 (잔여 개수, 만료일 등)
     */
    @Transactional(readOnly = true)
    public AdvertiserMyAccountResponseDto getMyAccountInfo(Long userId) {
        User advertiser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AdvertiserAuthority authority = advertiserAuthorityRepository.findByUser(advertiser)
                .orElseThrow(() -> new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED));

        long currentAdCount = nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(advertiser);
        long pendingCount = adProposalRepository.countByAdvertiserAndStatus(advertiser, AdProposalStatus.PENDING);

        return AdvertiserMyAccountResponseDto.of(authority, currentAdCount, pendingCount);
    }

    /**
     * 광고 신청 (PENDING)
     */
    @Transactional
    public void createProposal(Long userId, AdProposalRequestDto requestDto) {
        User advertiser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        AdvertiserAuthority authority = getValidAuthority(advertiser);
        checkAdCountLimit(advertiser, authority);

        Point point = geometryFactory.createPoint(new Coordinate(requestDto.getLongitude(), requestDto.getLatitude()));

        AdProposal proposal = AdProposal.builder()
                .advertiser(advertiser)
                .point(point)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .unlockRadius(requestDto.getUnlockRadius())
                .imageUrls(requestDto.getImageUrls())
                .categoryIds(requestDto.getCategoryIds())
                .status(AdProposalStatus.PENDING)
                .build();

        adProposalRepository.save(proposal);
        log.info("광고 신청 완료: Advertiser={}, Title={}", userId, requestDto.getTitle());
    }

    /**
     * 광고 신청 수정 (재심사 요청)
     */
    @Transactional
    public void updateProposal(Long userId, Long proposalId, AdProposalRequestDto requestDto) {
        AdProposal proposal = adProposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (!proposal.getAdvertiser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        // PENDING 또는 REJECTED 상태만 수정 가능
        if (proposal.getStatus() == AdProposalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        User advertiser = proposal.getAdvertiser();
        AdvertiserAuthority authority = getValidAuthority(advertiser);

        // REJECTED 상태에서 PENDING으로 변경되는 경우에만 개수 체크 (기존 PENDING은 이미 카운트에 포함됨)
        if (proposal.getStatus() == AdProposalStatus.REJECTED) {
            checkAdCountLimit(advertiser, authority);
        }

        Point point = geometryFactory.createPoint(new Coordinate(requestDto.getLongitude(), requestDto.getLatitude()));
        proposal.setPoint(point);
        proposal.setTitle(requestDto.getTitle());
        proposal.setContent(requestDto.getContent());
        proposal.setUnlockRadius(requestDto.getUnlockRadius());
        proposal.setImageUrls(requestDto.getImageUrls());
        proposal.setCategoryIds(requestDto.getCategoryIds());
        
        // 다시 심사 대기 상태로 변경
        proposal.setStatus(AdProposalStatus.PENDING);
        proposal.setRejectReason(null);

        log.info("광고 신청 수정 및 재심사 요청 완료: ProposalId={}", proposalId);
    }

    private AdvertiserAuthority getValidAuthority(User advertiser) {
        AdvertiserAuthority authority = advertiserAuthorityRepository.findByUser(advertiser)
                .orElseThrow(() -> new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED));

        // 권한 만료 체크
        if (authority.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ADVERTISER_AUTHORITY_EXPIRED);
        }
        return authority;
    }

    private void checkAdCountLimit(User advertiser, AdvertiserAuthority authority) {
        // 발행 가능 개수 체크 (현재 승인된 광고 수 + 신청 중인 광고 수)
        long currentAdCount = nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(advertiser);
        long pendingCount = adProposalRepository.countByAdvertiserAndStatus(advertiser, AdProposalStatus.PENDING);

        if (currentAdCount + pendingCount >= authority.getAllowedAdCount()) {
            throw new BusinessException(ErrorCode.AD_COUNT_LIMIT_EXCEEDED);
        }
    }

    /**
     * 내 광고 신청 내역 조회
     */
    @Transactional(readOnly = true)
    public List<AdProposalResponseDto> getMyProposals(Long userId) {
        User advertiser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<AdProposal> proposals = adProposalRepository.findAllByAdvertiser(advertiser);

        // Extract all unique category IDs from all proposals
        List<Long> allCategoryIds = proposals.stream()
                .flatMap(p -> p.getCategoryIds().stream())
                .distinct()
                .toList();

        // Fetch category names and create a map (ID -> Name)
        Map<Long, String> categoryMap = categoryRepository.findAllById(allCategoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return proposals.stream()
                .map(proposal -> {
                    List<String> categoryNames = proposal.getCategoryIds().stream()
                            .map(categoryMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                    return AdProposalResponseDto.from(proposal, categoryNames);
                })
                .toList();
    }

    /**
     * 발행된 내 광고 둥지 목록 조회 (만료/삭제된 광고 포함)
     */
    @Transactional(readOnly = true)
    public List<NestSimpleResponseDto> getMyAdNests(Long userId) {
        User advertiser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // SoftDeleteFilterAspect에 의해 nestFilter가 비활성화된 상태이므로 모든 광고가 조회됨
        return nestRepository.findAllByCreatorAndIsAdTrue(advertiser).stream()
                .map(NestSimpleResponseDto::from)
                .toList();
    }

    /**
     * 광고 성과 통계 조회 (만료/삭제된 광고 포함)
     */
    @Transactional(readOnly = true)
    public AdStatisticsResponseDto getAdStatistics(Long userId, Long nestId) {
        // SoftDeleteFilterAspect에 의해 nestFilter가 비활성화된 상태이므로 삭제된 둥지도 조회됨
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.getCreator().getId().equals(userId) || !nest.isAd()) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        NestAdInfo adInfo = nestAdInfoRepository.findByNestId(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        return AdStatisticsResponseDto.of(
                nest.getId(),
                nest.getTitle(),
                adInfo.getImpressions(),
                adInfo.getClicks(),
                adInfo.getExpiredAt()
        );
    }
}

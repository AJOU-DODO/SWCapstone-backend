package com.dodo.dodoserver.domain.ad.service;

import com.dodo.dodoserver.domain.ad.dao.AdProposalRepository;
import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.ad.dto.AdProposalRequestDto;
import com.dodo.dodoserver.domain.ad.dto.AdProposalResponseDto;
import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdvertiserAdServiceTest {

    @InjectMocks
    private AdvertiserAdService advertiserAdService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private AdvertiserAuthorityRepository advertiserAuthorityRepository;
    @Mock
    private AdProposalRepository adProposalRepository;
    @Mock
    private NestRepository nestRepository;
    @Mock
    private NestAdInfoRepository nestAdInfoRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private User user;
    private AdvertiserAuthority authority;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).nickname("광고주").build();
        authority = AdvertiserAuthority.builder()
                .user(user)
                .allowedAdCount(3)
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Test
    @DisplayName("광고 신청 성공")
    void createProposal_success() {
        AdProposalRequestDto requestDto = new AdProposalRequestDto();
        requestDto.setTitle("신청 제목");
        requestDto.setContent("신청 내용");
        requestDto.setLatitude(37.5);
        requestDto.setLongitude(127.0);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(advertiserAuthorityRepository.findByUser(user)).willReturn(Optional.of(authority));
        given(nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(user)).willReturn(1L);
        given(adProposalRepository.countByAdvertiserAndStatus(user, AdProposalStatus.PENDING)).willReturn(0L);

        advertiserAdService.createProposal(user.getId(), requestDto);

        verify(adProposalRepository).save(any(AdProposal.class));
    }

    @Test
    @DisplayName("광고 신청 실패 - 허용 개수 초과")
    void createProposal_fail_limitExceeded() {
        AdProposalRequestDto requestDto = new AdProposalRequestDto();
        requestDto.setTitle("신청 제목");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(advertiserAuthorityRepository.findByUser(user)).willReturn(Optional.of(authority));
        given(nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(user)).willReturn(2L);
        given(adProposalRepository.countByAdvertiserAndStatus(user, AdProposalStatus.PENDING)).willReturn(1L);

        assertThatThrownBy(() -> advertiserAdService.createProposal(user.getId(), requestDto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AD_COUNT_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("광고 신청 수정 실패 - 반려된 신청서 재제출 시 허용 개수 초과")
    void updateProposal_fail_limitExceeded_whenResubmittingRejected() {
        // given
        Long proposalId = 100L;
        AdProposal proposal = AdProposal.builder()
                .id(proposalId)
                .advertiser(user)
                .status(AdProposalStatus.REJECTED)
                .build();
        AdProposalRequestDto requestDto = new AdProposalRequestDto();
        requestDto.setTitle("수정 제목");
        requestDto.setLatitude(37.5);
        requestDto.setLongitude(127.0);

        given(adProposalRepository.findById(proposalId)).willReturn(Optional.of(proposal));
        given(advertiserAuthorityRepository.findByUser(user)).willReturn(Optional.of(authority));
        given(nestRepository.countByCreatorAndIsAdTrueAndDeletedAtIsNull(user)).willReturn(3L); // 이미 한도 도달
        given(adProposalRepository.countByAdvertiserAndStatus(user, AdProposalStatus.PENDING)).willReturn(0L);

        // when & then
        assertThatThrownBy(() -> advertiserAdService.updateProposal(user.getId(), proposalId, requestDto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AD_COUNT_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("광고 신청 수정 실패 - 권한 만료")
    void updateProposal_fail_authorityExpired() {
        // given
        Long proposalId = 100L;
        AdProposal proposal = AdProposal.builder()
                .id(proposalId)
                .advertiser(user)
                .status(AdProposalStatus.REJECTED)
                .build();
        
        // 만료된 권한 생성
        AdvertiserAuthority expiredAuthority = AdvertiserAuthority.builder()
                .user(user)
                .allowedAdCount(3)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .build();
        
        AdProposalRequestDto requestDto = new AdProposalRequestDto();
        requestDto.setLatitude(37.5);
        requestDto.setLongitude(127.0);

        given(adProposalRepository.findById(proposalId)).willReturn(Optional.of(proposal));
        given(advertiserAuthorityRepository.findByUser(user)).willReturn(Optional.of(expiredAuthority));

        // when & then
        assertThatThrownBy(() -> advertiserAdService.updateProposal(user.getId(), proposalId, requestDto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADVERTISER_AUTHORITY_EXPIRED);
    }

    @Test
    @DisplayName("내 광고 신청 내역 조회 성공 - 카테고리 ID가 null인 경우 포함")
    void getMyProposals_success_withNullCategoryIds() {
        // given
        AdProposal p1 = AdProposal.builder().id(1L).advertiser(user).categoryIds(List.of(1L)).build();
        AdProposal p2 = AdProposal.builder().id(2L).advertiser(user).categoryIds(null).build(); // null 카테고리
        
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(adProposalRepository.findAllByAdvertiser(user)).willReturn(List.of(p1, p2));
        given(categoryRepository.findAllById(any())).willReturn(List.of());

        // when
        List<AdProposalResponseDto> result = advertiserAdService.getMyProposals(user.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryNames()).isEmpty();
        assertThat(result.get(1).getCategoryNames()).isEmpty();
    }
}

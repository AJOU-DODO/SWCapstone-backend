package com.dodo.dodoserver.domain.ad.service;

import com.dodo.dodoserver.domain.ad.dao.AdProposalRepository;
import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.ad.dto.AdProposalRequestDto;
import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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
                .isInstanceOf(BusinessException.class);
    }
}

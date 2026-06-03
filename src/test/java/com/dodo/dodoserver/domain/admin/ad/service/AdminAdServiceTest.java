package com.dodo.dodoserver.domain.admin.ad.service;

import com.dodo.dodoserver.domain.ad.dao.AdProposalRepository;
import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.admin.ad.dto.AdApproveRequestDto;
import com.dodo.dodoserver.domain.admin.ad.dto.AdvertiserAuthorityRequestDto;
import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.NestCategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAdServiceTest {

    @InjectMocks
    private AdminAdService adminAdService;

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
    @Mock
    private NestCategoryRepository nestCategoryRepository;

    private User user;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("advertiser@test.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("광고주 권한 부여 성공")
    void grantAdvertiserRole_success() {
        AdvertiserAuthorityRequestDto requestDto = new AdvertiserAuthorityRequestDto();
        requestDto.setAllowedAdCount(5);
        requestDto.setExpiredAt(LocalDateTime.now().plusMonths(1));

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(advertiserAuthorityRepository.findByUser(user)).willReturn(Optional.empty());

        adminAdService.grantAdvertiserRole(user.getId(), requestDto);

        assertThat(user.getRole()).isEqualTo(Role.ADVERTISER);
        verify(advertiserAuthorityRepository).save(any(AdvertiserAuthority.class));
    }

    @Test
    @DisplayName("광고 신청 승인 성공")
    void approveProposal_success() {
        AdProposal proposal = AdProposal.builder()
                .id(10L)
                .advertiser(user)
                .title("광고 제목")
                .content("광고 내용")
                .point(geometryFactory.createPoint(new Coordinate(127.0, 37.5)))
                .status(AdProposalStatus.PENDING)
                .build();

        AdApproveRequestDto requestDto = new AdApproveRequestDto();
        requestDto.setExpiredAt(LocalDateTime.now().plusMonths(1));
        requestDto.setPriorityScore(10);

        given(adProposalRepository.findById(10L)).willReturn(Optional.of(proposal));
        given(nestRepository.save(any(Nest.class))).willAnswer(inv -> inv.getArgument(0));

        adminAdService.approveProposal(10L, requestDto);

        verify(nestRepository).save(any(Nest.class));
        verify(nestAdInfoRepository).save(any());
        verify(adProposalRepository).delete(proposal);
    }
}

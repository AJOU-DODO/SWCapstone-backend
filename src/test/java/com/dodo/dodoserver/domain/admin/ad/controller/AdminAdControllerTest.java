package com.dodo.dodoserver.domain.admin.ad.controller;

import com.dodo.dodoserver.domain.admin.ad.dto.AdApproveRequestDto;
import com.dodo.dodoserver.domain.admin.ad.dto.AdProposalAdminResponseDto;
import com.dodo.dodoserver.domain.admin.ad.dto.AdvertiserAuthorityRequestDto;
import com.dodo.dodoserver.domain.admin.ad.service.AdminAdService;
import com.dodo.dodoserver.global.config.AppProperties;
import com.dodo.dodoserver.global.config.SecurityConfig;
import com.dodo.dodoserver.global.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAdController.class)
@Import(SecurityConfig.class)
class AdminAdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminAdService adminAdService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AppProperties appProperties;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("광고주 권한 부여 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void grantAdvertiserRole_success() throws Exception {
        AdvertiserAuthorityRequestDto requestDto = new AdvertiserAuthorityRequestDto();
        requestDto.setAllowedAdCount(5);
        requestDto.setExpiredAt(LocalDateTime.now().plusMonths(1));

        mockMvc.perform(post("/api/v1/admin/ads/advertisers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("대기 중인 광고 신청 목록 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getPendingProposals_success() throws Exception {
        AdProposalAdminResponseDto responseDto = AdProposalAdminResponseDto.builder()
                .id(1L)
                .title("광고 신청")
                .advertiserNickname("광고주")
                .build();

        given(adminAdService.getPendingProposals()).willReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v1/admin/ads/proposals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].title").value("광고 신청"));
    }

    @Test
    @DisplayName("광고 신청 승인 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void approveProposal_success() throws Exception {
        AdApproveRequestDto requestDto = new AdApproveRequestDto();
        requestDto.setExpiredAt(LocalDateTime.now().plusMonths(1));

        mockMvc.perform(post("/api/v1/admin/ads/proposals/1/approve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

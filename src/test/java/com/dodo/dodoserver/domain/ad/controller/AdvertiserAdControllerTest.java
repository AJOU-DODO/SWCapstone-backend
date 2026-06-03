package com.dodo.dodoserver.domain.ad.controller;

import com.dodo.dodoserver.domain.ad.dto.AdProposalRequestDto;
import com.dodo.dodoserver.domain.ad.dto.AdProposalResponseDto;
import com.dodo.dodoserver.domain.ad.dto.AdvertiserMyAccountResponseDto;
import com.dodo.dodoserver.domain.ad.service.AdvertiserAdService;
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

@WebMvcTest(AdvertiserAdController.class)
@Import(SecurityConfig.class)
class AdvertiserAdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvertiserAdService advertiserAdService;

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
    @DisplayName("광고주 계정 정보 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADVERTISER")
    void getMyAccountInfo_success() throws Exception {
        AdvertiserMyAccountResponseDto responseDto = AdvertiserMyAccountResponseDto.builder()
                .allowedAdCount(5)
                .remainingAdCount(3L)
                .expiredAt(LocalDateTime.now().plusMonths(1))
                .build();

        given(advertiserAdService.getMyAccountInfo(any())).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/advertiser/ads/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.allowedAdCount").value(5));
    }

    @Test
    @DisplayName("광고 신청 성공")
    @WithMockUserPrincipal(role = "ROLE_ADVERTISER")
    void createProposal_success() throws Exception {
        AdProposalRequestDto requestDto = new AdProposalRequestDto();
        requestDto.setTitle("새 광고");
        requestDto.setContent("내용");
        requestDto.setLatitude(37.5);
        requestDto.setLongitude(127.0);

        mockMvc.perform(post("/api/v1/advertiser/ads/proposals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("내 광고 신청 내역 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADVERTISER")
    void getMyProposals_success() throws Exception {
        AdProposalResponseDto responseDto = AdProposalResponseDto.builder()
                .id(1L)
                .title("신청 광고")
                .build();

        given(advertiserAdService.getMyProposals(any())).willReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v1/advertiser/ads/proposals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].title").value("신청 광고"));
    }
}

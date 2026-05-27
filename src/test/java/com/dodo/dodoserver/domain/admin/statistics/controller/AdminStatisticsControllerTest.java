package com.dodo.dodoserver.domain.admin.statistics.controller;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminPostcardRatioResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.dto.AdminTrendResponseDto;
import com.dodo.dodoserver.domain.admin.statistics.service.AdminStatisticsService;
import com.dodo.dodoserver.global.config.AppProperties;
import com.dodo.dodoserver.global.config.SecurityConfig;
import com.dodo.dodoserver.global.security.CustomOAuth2UserService;
import com.dodo.dodoserver.global.security.JwtAuthenticationFilter;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.OAuth2AuthenticationSuccessHandler;
import com.dodo.dodoserver.global.security.WithMockUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminStatisticsController.class)
@Import(SecurityConfig.class)
class AdminStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminStatisticsService adminStatisticsService;

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
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("통계 요약 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getSummaryStats_Success() throws Exception {
        // given
        AdminSummaryResponseDto response = AdminSummaryResponseDto.builder()
                .totalNests(100L)
                .todayNests(10L)
                .build();
        given(adminStatisticsService.getSummaryStats()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/admin/statistics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalNests").value(100));
    }

    @Test
    @DisplayName("엽서 교환 비율 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getPostcardRatioStats_Success() throws Exception {
        // given
        AdminPostcardRatioResponseDto response = AdminPostcardRatioResponseDto.builder()
                .totalGenerated(100L)
                .totalDelivered(20L)
                .deliveryRatio(20.0)
                .build();
        given(adminStatisticsService.getPostcardRatioStats(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/admin/statistics/postcards/ratio")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.deliveryRatio").value(20.0));
    }

    @Test
    @DisplayName("트래픽 트렌드 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getTrafficTrends_Success() throws Exception {
        // given
        given(adminStatisticsService.getTrafficTrends(any(), any())).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/admin/statistics/trends")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("통계 조회 실패 - 권한 없음 (USER)")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void getSummaryStats_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics/summary"))
                .andExpect(status().isForbidden());
    }
}

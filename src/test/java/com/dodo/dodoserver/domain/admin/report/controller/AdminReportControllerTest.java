package com.dodo.dodoserver.domain.admin.report.controller;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.ReportDetailResponseDto;
import com.dodo.dodoserver.domain.admin.report.service.AdminReportService;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.global.config.SecurityConfig;
import com.dodo.dodoserver.global.security.CustomOAuth2UserService;
import com.dodo.dodoserver.global.security.JwtAuthenticationFilter;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.OAuth2AuthenticationSuccessHandler;
import com.dodo.dodoserver.global.security.WithMockUserPrincipal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReportController.class)
@Import(SecurityConfig.class)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReportService adminReportService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
    @DisplayName("신고된 둥지 목록 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getReportedNests_success() throws Exception {
        // given
        AdminNestReportResponseDto responseDto = AdminNestReportResponseDto.builder()
                .nestId(1L)
                .authorNickname("산책왕")
                .content("욕설 내용")
                .reportCount(5)
                .reasons(Collections.singletonList(ReportReason.ABUSE))
                .status(ReportStatus.PENDING)
                .build();
        Page<AdminNestReportResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1);
        given(adminReportService.getReportedNests(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/admin/reports/nests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].authorNickname").value("산책왕"));
    }

    @Test
    @DisplayName("신고 상세 정보 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getReportDetails_success() throws Exception {
        // given
        ReportDetailResponseDto responseDto = ReportDetailResponseDto.builder()
                .targetType(ReportType.NEST)
                .targetId(1L)
                .stats(Map.of("pendingAbuseCount", 5L))
                .otherReportContents(Collections.singletonList("기타 신고 내용"))
                .build();
        given(adminReportService.getReportDetails(eq(ReportType.NEST), eq(1L))).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/admin/reports/details")
                        .param("targetType", "NEST")
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stats.pendingAbuseCount").value(5));
    }
}

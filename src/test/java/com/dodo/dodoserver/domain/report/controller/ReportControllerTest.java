package com.dodo.dodoserver.domain.report.controller;

import com.dodo.dodoserver.domain.report.dto.ReportRequestDto;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.domain.report.service.ReportService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

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
    @DisplayName("신고 접수 성공 - 유저 권한")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void createReport_success() throws Exception {
        // given
        ReportRequestDto requestDto = ReportRequestDto.builder()
                .reportType(ReportType.NEST)
                .targetId(1L)
                .reason(ReportReason.ABUSE)
                .content("욕설이 포함되어 있습니다.")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("신고 접수 실패 - 입력값 누락")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void createReport_fail_invalidInput() throws Exception {
        // given
        ReportRequestDto requestDto = ReportRequestDto.builder()
                .targetId(1L)
                .build(); // reportType, reason 누락

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}

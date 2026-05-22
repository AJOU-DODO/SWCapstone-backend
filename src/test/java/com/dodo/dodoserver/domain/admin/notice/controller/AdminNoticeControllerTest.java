package com.dodo.dodoserver.domain.admin.notice.controller;

import com.dodo.dodoserver.domain.admin.notice.dto.NoticeRequestDto;
import com.dodo.dodoserver.domain.admin.notice.service.AdminNoticeService;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import com.dodo.dodoserver.domain.notice.entity.NoticeCategory;
import com.dodo.dodoserver.global.config.AppProperties;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminNoticeController.class)
@Import(SecurityConfig.class)
class AdminNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminNoticeService adminNoticeService;

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
    @DisplayName("공지사항 등록 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void createNotice_success() throws Exception {
        // given
        String requestBody = "{\"category\":\"UPDATE\",\"title\":\"제목\",\"content\":\"내용\"}";
        given(adminNoticeService.createNotice(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/v1/admin/notices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("공지사항 발행 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void publishNotice_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/admin/notices/1/publish")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("관리자 공지사항 목록 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getAllNotices_success() throws Exception {
        // given
        NoticeResponseDto responseDto = NoticeResponseDto.builder()
                .id(1L)
                .title("제목")
                .category(NoticeCategory.UPDATE)
                .build();
        given(adminNoticeService.getAllNotices(any())).willReturn(new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/api/v1/admin/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"));
    }

    @Test
    @DisplayName("관리자 API 접근 실패 - 일반 유저 권한")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void adminApi_fail_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/notices"))
                .andExpect(status().isForbidden());
    }
}

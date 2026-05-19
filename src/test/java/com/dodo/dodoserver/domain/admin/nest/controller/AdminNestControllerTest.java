package com.dodo.dodoserver.domain.admin.nest.controller;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDetailResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.admin.nest.service.AdminNestService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminNestController.class)
@Import(SecurityConfig.class)
class AdminNestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminNestService adminNestService;

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
    @DisplayName("어드민 게시물 목록 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getNests_success() throws Exception {
        // given
        AdminNestResponseDto responseDto = AdminNestResponseDto.builder()
                .nestId(1L)
                .authorNickname("도도새")
                .content("둥지 내용")
                .createdAt(LocalDateTime.now())
                .build();
        Page<AdminNestResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1);
        given(adminNestService.getNests(any(), any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/admin/nests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].authorNickname").value("도도새"));
    }

    @Test
    @DisplayName("어드민 게시물 상세 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getNestDetail_success() throws Exception {
        // given
        AdminNestDetailResponseDto responseDto = AdminNestDetailResponseDto.builder()
                .nestId(1L)
                .title("제목")
                .content("원본 본문")
                .authorNickname("산책왕")
                .latitude(37.2844)
                .longitude(127.0442)
                .build();
        given(adminNestService.getNestDetail(1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/admin/nests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").value("원본 본문"));
    }

    @Test
    @DisplayName("어드민 게시물 삭제 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void deleteNest_success() throws Exception {
        // ... (existing test)
        mockMvc.perform(delete("/api/v1/admin/nests/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"운영 정책 위반\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("어드민 둥지별 댓글 목록 조회 성공 - 트리 구조 검증")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getNestComments_success() throws Exception {
        // given
        AdminCommentResponseDto child = AdminCommentResponseDto.builder()
                .commentId(2L)
                .parentId(1L)
                .content("대댓글")
                .build();
        AdminCommentResponseDto parent = AdminCommentResponseDto.builder()
                .commentId(1L)
                .content("댓글")
                .children(Collections.singletonList(child))
                .build();
        given(adminNestService.getNestComments(1L)).willReturn(Collections.singletonList(parent));

        // when & then
        mockMvc.perform(get("/api/v1/admin/nests/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].commentId").value(1))
                .andExpect(jsonPath("$.data[0].children[0].commentId").value(2));
    }
}

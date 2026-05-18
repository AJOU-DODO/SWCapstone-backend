package com.dodo.dodoserver.domain.admin.user.controller;

import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistRequestDto;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistResponseDto;
import com.dodo.dodoserver.domain.admin.user.service.AdminEmailWhitelistService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminEmailWhitelistController.class)
@Import(SecurityConfig.class)
class AdminEmailWhitelistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminEmailWhitelistService adminEmailWhitelistService;

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
    @DisplayName("화이트리스트 목록 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getWhitelists_success() throws Exception {
        // given
        AdminEmailWhitelistResponseDto responseDto = AdminEmailWhitelistResponseDto.builder()
                .id(1L)
                .email("admin@dodo.com")
                .remark("운영자")
                .build();
        given(adminEmailWhitelistService.getWhitelists()).willReturn(List.of(responseDto));

        // when & then
        mockMvc.perform(get("/api/v1/admin/whitelists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].email").value("admin@dodo.com"));
    }

    @Test
    @DisplayName("화이트리스트 추가 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void addWhitelist_success() throws Exception {
        // given
        AdminEmailWhitelistRequestDto requestDto = new AdminEmailWhitelistRequestDto("new@dodo.com", "비고");
        AdminEmailWhitelistResponseDto responseDto = AdminEmailWhitelistResponseDto.builder()
                .id(1L)
                .email("new@dodo.com")
                .remark("비고")
                .build();
        given(adminEmailWhitelistService.addWhitelist(any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/admin/whitelists")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("new@dodo.com"));
    }

    @Test
    @DisplayName("화이트리스트 삭제 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void removeWhitelist_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/whitelists/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("화이트리스트 조회 실패 - 일반 유저")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void getWhitelists_fail_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/whitelists"))
                .andExpect(status().isForbidden());
    }
}

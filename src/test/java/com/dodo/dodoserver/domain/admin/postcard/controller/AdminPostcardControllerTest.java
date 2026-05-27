package com.dodo.dodoserver.domain.admin.postcard.controller;

import com.dodo.dodoserver.domain.admin.postcard.dto.AdminPostcardDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.postcard.service.AdminPostcardService;
import com.dodo.dodoserver.domain.admin.report.dto.AdminPostcardReportResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPostcardController.class)
@Import(SecurityConfig.class)
class AdminPostcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminPostcardService adminPostcardService;

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
    @DisplayName("신고된 엽서 목록 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getReportedPostcards_success() throws Exception {
        // given
        AdminPostcardReportResponseDto responseDto = AdminPostcardReportResponseDto.builder()
                .postcardId(1L)
                .authorNickname("유저1")
                .build();
        given(adminPostcardService.getReportedPostcards(any(), any()))
                .willReturn(new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/api/v1/admin/postcards/reported")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "RECENT_REPORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].authorNickname").value("유저1"));
    }

    @Test
    @DisplayName("관리자 전용 엽서 삭제 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void deletePostcard_success() throws Exception {
        // given
        AdminPostcardDeleteRequestDto requestDto = new AdminPostcardDeleteRequestDto();

        // when & then
        mockMvc.perform(delete("/api/v1/admin/postcards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

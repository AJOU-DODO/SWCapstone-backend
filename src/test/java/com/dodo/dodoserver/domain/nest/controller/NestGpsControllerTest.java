package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestService;
import com.dodo.dodoserver.global.config.SecurityConfig;
import com.dodo.dodoserver.global.security.CustomOAuth2UserService;
import com.dodo.dodoserver.global.security.JwtAuthenticationFilter;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.OAuth2AuthenticationSuccessHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NestGpsController.class)
@Import(SecurityConfig.class)
class NestGpsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NestService nestService;

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
    @DisplayName("둥지 해금 성공")
    @WithMockUser
    void unlockNest_success() throws Exception {
        Long nestId = 1L;
        NestUnlockRequestDto requestDto = new NestUnlockRequestDto(37.5, 127.0);

        mockMvc.perform(post("/api/v1/nests/{id}/unlock", nestId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("둥지가 성공적으로 해금되었습니다."));
    }

    @Test
    @DisplayName("둥지 요약 정보 리스트 조회 성공")
    @WithMockUser
    void getNestsByIds_success() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        NestSummaryResponseDto summary = NestSummaryResponseDto.builder().id(1L).title("테스트").build();
        given(nestService.getNestsByIds(any(), eq(ids))).willReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/nests/summaries")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("반경 내 둥지 목록 조회 성공")
    @WithMockUser
    void getNearbyNests_success() throws Exception {
        NestSummaryResponseDto summary = NestSummaryResponseDto.builder().id(1L).title("테스트").build();
        Page<NestSummaryResponseDto> page = new PageImpl<>(List.of(summary));
        
        given(nestService.getNearNestsByCategory(any(), any(), any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/nests")
                        .param("latitude", "37.5")
                        .param("longitude", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("주변 핀 조회 성공")
    @WithMockUser
    void getNearbyPins_success() throws Exception {
        NestPinResponseDto pin = new NestPinResponseDto(1L, 37.5, 127.0);
        given(nestService.getNearbyPins(any(), any(), any())).willReturn(List.of(pin));

        mockMvc.perform(get("/api/v1/nests/pins")
                        .param("latitude", "37.5")
                        .param("longitude", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

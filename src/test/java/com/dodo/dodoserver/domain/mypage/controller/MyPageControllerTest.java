package com.dodo.dodoserver.domain.mypage.controller;

import com.dodo.dodoserver.domain.mypage.dto.*;
import com.dodo.dodoserver.domain.mypage.service.MyPageService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPageController.class)
@Import(SecurityConfig.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageService myPageService;

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
    @DisplayName("나의 작성 둥지 목록 조회 성공")
    @WithMockUserPrincipal
    void getMyNests_success() throws Exception {
        MyPageNestResponseDto responseDto = MyPageNestResponseDto.builder().id(1L).title("제목").build();
        Page<MyPageNestResponseDto> page = new PageImpl<>(List.of(responseDto));

        given(myPageService.getMyNests(anyLong(), any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/mypage/nests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("제목"));
    }

    @Test
    @DisplayName("활동 통계 조회 성공")
    @WithMockUserPrincipal
    void getMyStatistics_success() throws Exception {
        MyPageStatisticsResponseDto responseDto = MyPageStatisticsResponseDto.builder()
                .nestCount(5)
                .commentCount(10)
                .postcardCount(3)
                .build();

        given(myPageService.getMyStatistics(anyLong())).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/mypage/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nestCount").value(5))
                .andExpect(jsonPath("$.data.commentCount").value(10));
    }

    @Test
    @DisplayName("보유 엽서 목록 조회 성공")
    @WithMockUserPrincipal
    void getMyPostcards_success() throws Exception {
        MyPagePostcardResponseDto responseDto = MyPagePostcardResponseDto.builder().id(1L).content("엽서내용").build();
        Page<MyPagePostcardResponseDto> page = new PageImpl<>(List.of(responseDto));

        given(myPageService.getMyPostcards(anyLong(), anyString(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/mypage/postcards")
                        .param("filter", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("엽서내용"));
    }
}

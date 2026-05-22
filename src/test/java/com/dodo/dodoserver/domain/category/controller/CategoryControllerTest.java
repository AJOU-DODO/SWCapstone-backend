package com.dodo.dodoserver.domain.category.controller;

import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.service.CategoryService;
import com.dodo.dodoserver.global.config.AppProperties;
import com.dodo.dodoserver.global.security.CustomOAuth2UserService;
import com.dodo.dodoserver.global.security.JwtAuthenticationFilter;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.dodo.dodoserver.global.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.dodo.dodoserver.global.security.WithMockUserPrincipal;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.doAnswer;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    // SecurityConfig에 필요한 MockitoBean들
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

    /**
     * JwtAuthenticationFilter를 MockitoBean으로 주입하면 실제 필터 로직이 실행되지 않음.
     * 필터 체인이 중단되지 않고 다음 필터나 컨트롤러로 요청이 전달되도록 doFilter 호출을 스터빙함.
     */
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
    @DisplayName("전체 카테고리 조회 성공 - 인증된 사용자")
    @WithMockUserPrincipal
    void getAllCategories_success() throws Exception {
        // given
        CategoryResponseDto res1 = new CategoryResponseDto(1L, "운동", LocalDateTime.now());
        CategoryResponseDto res2 = new CategoryResponseDto(2L, "공부", LocalDateTime.now());
        given(categoryService.getAllCategories()).willReturn(List.of(res1, res2));

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("운동"))
                .andExpect(jsonPath("$.data[1].name").value("공부"));
    }
}

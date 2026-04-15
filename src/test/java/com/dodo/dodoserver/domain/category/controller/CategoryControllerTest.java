package com.dodo.dodoserver.domain.category.controller;

import com.dodo.dodoserver.domain.category.dto.CategoryRequestDto;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.service.CategoryService;
import com.dodo.dodoserver.global.security.CustomOAuth2UserService;
import com.dodo.dodoserver.global.security.JwtAuthenticationFilter;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.OAuth2AuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.dodo.dodoserver.global.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * JwtAuthenticationFilter를 MockitoBean으로 주입하면 실제 필터 로직이 실행되지 않음.
     * 필터 체인이 중단되지 않고 다음 필터나 컨트롤러로 요청이 전달되도록 doFilter 호출을 스터빙함.
     */
    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            // 메서드 호출 시 전달된 인자 추출 (0: Request, 1: Response, 2: FilterChain)
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);

            // 가짜 필터 로직 대신 다음 필터 체인으로 요청을 강제 전달
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("카테고리 생성 성공 - ADMIN 권한 필요")
    @WithMockUser(roles = "ADMIN")
    void createCategory_success() throws Exception {
        // given
        CategoryRequestDto requestDto = new CategoryRequestDto("운동");
        CategoryResponseDto responseDto = new CategoryResponseDto(1L, "운동", LocalDateTime.now());

        given(categoryService.createCategory(any(CategoryRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("운동"));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 권한 없음(USER)")
    @WithMockUser(roles = "USER")
    void createCategory_fail_forbidden() throws Exception {
        // given
        CategoryRequestDto requestDto = new CategoryRequestDto("운동");

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공 - 인증된 사용자")
    @WithMockUser
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

    @Test
    @DisplayName("카테고리 수정 성공 - ADMIN 권한")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_success() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryRequestDto requestDto = new CategoryRequestDto("독서");
        CategoryResponseDto responseDto = new CategoryResponseDto(categoryId, "독서", LocalDateTime.now());

        given(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/v1/categories/{id}", categoryId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("독서"));
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - ADMIN 권한")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success() throws Exception {
        // given
        Long categoryId = 1L;

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("카테고리가 성공적으로 삭제되었습니다."));
    }
}

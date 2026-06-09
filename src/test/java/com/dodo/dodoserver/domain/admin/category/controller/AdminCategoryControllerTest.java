package com.dodo.dodoserver.domain.admin.category.controller;

import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryRequestDto;
import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;
import com.dodo.dodoserver.domain.admin.category.dto.CategoryOrderUpdateRequestDto;
import com.dodo.dodoserver.domain.admin.category.service.AdminCategoryService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@Import(SecurityConfig.class)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminCategoryService adminCategoryService;

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
    @DisplayName("어드민 카테고리 목록 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getCategories_success() throws Exception {
        // given
        AdminCategoryResponseDto responseDto = AdminCategoryResponseDto.builder()
                .id(1L)
                .name("테스트 카테고리")
                .sortOrder(0)
                .build();
        given(adminCategoryService.getCategories(true, null)).willReturn(List.of(responseDto));

        // when & then
        mockMvc.perform(get("/api/v1/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("테스트 카테고리"));
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void createCategory_success() throws Exception {
        // given
        AdminCategoryRequestDto requestDto = new AdminCategoryRequestDto("새 카테고리");
        AdminCategoryResponseDto responseDto = AdminCategoryResponseDto.builder()
                .id(1L)
                .name("새 카테고리")
                .sortOrder(1)
                .build();
        given(adminCategoryService.createCategory(any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("새 카테고리"));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void updateCategory_success() throws Exception {
        // given
        AdminCategoryRequestDto requestDto = new AdminCategoryRequestDto("수정된 카테고리");

        // when & then
        mockMvc.perform(patch("/api/v1/admin/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("카테고리 순서 수정 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void updateCategoryOrders_success() throws Exception {
        // given
        CategoryOrderUpdateRequestDto.CategoryOrderDto orderDto = 
                new CategoryOrderUpdateRequestDto.CategoryOrderDto(1L, 1);
        CategoryOrderUpdateRequestDto requestDto = 
                new CategoryOrderUpdateRequestDto(List.of(orderDto));

        // when & then
        mockMvc.perform(put("/api/v1/admin/categories/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void deleteCategory_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/categories/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

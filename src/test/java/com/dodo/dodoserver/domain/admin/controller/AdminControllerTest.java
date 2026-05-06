package com.dodo.dodoserver.domain.admin.controller;

import com.dodo.dodoserver.domain.admin.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.admin.dto.UserSanctionRequestDto;
import com.dodo.dodoserver.domain.admin.entity.SanctionType;
import com.dodo.dodoserver.domain.admin.service.AdminService;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.error.ErrorCode;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

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
    @DisplayName("유저 목록 조회 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getAllUsers_success() throws Exception {
        // given
        UserAdminResponseDto responseDto = UserAdminResponseDto.builder()
                .id(1L)
                .nickname("테스트유저")
                .email("test@example.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .nestCount(5L)
                .commentCount(10L)
                .isSanctioned(false)
                .build();
        Page<UserAdminResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1);
        given(adminService.getAllUsers(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("유저 목록 조회 실패 - 일반 유저 권한")
    @WithMockUserPrincipal(role = "ROLE_USER")
    void getAllUsers_fail_forbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유저 제재 성공 - 관리자 권한")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void sanctionUser_success() throws Exception {
        // given
        UserSanctionRequestDto requestDto = new UserSanctionRequestDto();
        requestDto.setSanctionType(SanctionType.SEVEN_DAYS);
        requestDto.setReason("부적절한 닉네임");

        // when & then
        mockMvc.perform(post("/api/v1/admin/users/1/sanction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("유저 제재 실패 - 입력값 누락")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void sanctionUser_fail_invalidInput() throws Exception {
        // given
        UserSanctionRequestDto requestDto = new UserSanctionRequestDto();
        // sanctionType, reason 누락

        // when & then
        mockMvc.perform(post("/api/v1/admin/users/1/sanction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(ErrorCode.INPUT_VALIDATION_ERROR.getCode()));
    }
}

package com.dodo.dodoserver.domain.user.controller;

import com.dodo.dodoserver.domain.user.dto.OnboardRequestDto;
import com.dodo.dodoserver.domain.user.dto.ProfileUpdateRequestDto;
import com.dodo.dodoserver.domain.user.dto.UserProfileResponseDto;
import com.dodo.dodoserver.domain.user.service.UserService;
import com.dodo.dodoserver.error.ErrorCode;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

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
    @DisplayName("내 프로필 조회 성공")
    @WithMockUser(username = "test@example.com")
    void getMyProfile_success() throws Exception {
        // given
        UserProfileResponseDto responseDto = UserProfileResponseDto.builder()
                .email("test@example.com")
                .nickname("테스터")
                .build();
        given(userService.getUserProfileByEmail("test@example.com")).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    @DisplayName("닉네임 중복 체크 성공")
    @WithMockUser
    void checkNickname_success() throws Exception {
        // given
        given(userService.existsByNickname("이미있는닉네임")).willReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/users/check-nickname")
                        .param("nickname", "이미있는닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("프로필 수정 성공")
    @WithMockUser(username = "test@example.com")
    void updateProfile_success() throws Exception {
        // given
        ProfileUpdateRequestDto requestDto = new ProfileUpdateRequestDto("새닉네임", null, "새소개");

        // when & then
        mockMvc.perform(patch("/api/v1/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("프로필 정보가 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("온보딩 성공")
    @WithMockUser(username = "new@example.com")
    void postProfile_success() throws Exception {
        // given
        OnboardRequestDto requestDto = new OnboardRequestDto(
                "신규유저", "fcm-token", "소개", null, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("온보딩이 완료되었습니다."));
    }

    @Test
    @DisplayName("온보딩 실패 - 입력값 검증 오류 (닉네임 누락)")
    @WithMockUser(username = "new@example.com")
    void postProfile_fail_invalidInput() throws Exception {
        // given
        OnboardRequestDto requestDto = new OnboardRequestDto(
                "", "fcm-token", "소개", null, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(ErrorCode.INPUT_VALIDATION_ERROR.getCode()));
    }

    @Test
    @DisplayName("특정 유저 상세 조회 성공")
    @WithMockUser
    void getUserProfile_success() throws Exception {
        // given
        String email = "other@example.com";
        UserProfileResponseDto responseDto = UserProfileResponseDto.builder()
                .email(email)
                .nickname("다른유저")
                .build();
        given(userService.getUserProfileByEmail(email)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/profile/detail")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value("다른유저"));
    }
}

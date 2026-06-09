package com.dodo.dodoserver.domain.admin.inquiry.controller;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryDetailResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.InquiryAnswerRequestDto;
import com.dodo.dodoserver.domain.admin.inquiry.service.AdminInquiryService;
import com.dodo.dodoserver.global.config.AppProperties;
import com.dodo.dodoserver.global.config.SecurityConfig;
import com.dodo.dodoserver.global.security.*;
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

@WebMvcTest(AdminInquiryController.class)
@Import(SecurityConfig.class)
class AdminInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminInquiryService adminInquiryService;

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
    @DisplayName("관리자 문의 리스트 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getInquiries_success() throws Exception {
        // given
        AdminInquiryResponseDto responseDto = AdminInquiryResponseDto.builder()
                .id(1L)
                .title("문의 제목")
                .userNickname("유저1")
                .build();
        Page<AdminInquiryResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1);
        given(adminInquiryService.getInquiries(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/admin/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].title").value("문의 제목"));
    }

    @Test
    @DisplayName("관리자 문의 상세 조회 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void getInquiryDetail_success() throws Exception {
        // given
        AdminInquiryDetailResponseDto responseDto = AdminInquiryDetailResponseDto.builder()
                .id(1L)
                .title("문의 제목")
                .content("문의 내용")
                .build();
        given(adminInquiryService.getInquiryDetail(1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/admin/inquiries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").value("문의 내용"));
    }

    @Test
    @DisplayName("관리자 문의 답변 등록 성공")
    @WithMockUserPrincipal(role = "ROLE_ADMIN")
    void answerInquiry_success() throws Exception {
        // given
        InquiryAnswerRequestDto requestDto = new InquiryAnswerRequestDto("답변 내용");

        // when & then
        mockMvc.perform(post("/api/v1/admin/inquiries/1/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

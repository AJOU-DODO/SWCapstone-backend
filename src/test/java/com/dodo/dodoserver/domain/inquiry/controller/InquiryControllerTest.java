package com.dodo.dodoserver.domain.inquiry.controller;

import com.dodo.dodoserver.domain.inquiry.dto.InquiryRequestDto;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryType;
import com.dodo.dodoserver.domain.inquiry.service.InquiryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@Import(SecurityConfig.class)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InquiryService inquiryService;

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
    @DisplayName("1:1 문의 등록 성공")
    @WithMockUserPrincipal
    void createInquiry_success() throws Exception {
        // given
        InquiryRequestDto requestDto = new InquiryRequestDto(InquiryType.BUG, "제목", "내용");
        InquiryResponseDto responseDto = InquiryResponseDto.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .build();
        given(inquiryService.createInquiry(any(), any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/inquiries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("제목"));
    }

    @Test
    @DisplayName("내 문의 리스트 조회 성공")
    @WithMockUserPrincipal
    void getMyInquiries_success() throws Exception {
        // given
        InquiryResponseDto responseDto = InquiryResponseDto.builder()
                .id(1L)
                .title("제목")
                .build();
        Page<InquiryResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto), PageRequest.of(0, 10), 1);
        given(inquiryService.getMyInquiries(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/inquiries/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"));
    }

    @Test
    @DisplayName("문의 수정 성공")
    @WithMockUserPrincipal
    void updateInquiry_success() throws Exception {
        // given
        InquiryRequestDto requestDto = new InquiryRequestDto(InquiryType.BUG, "수정 제목", "수정 내용");
        InquiryResponseDto responseDto = InquiryResponseDto.builder()
                .id(1L)
                .title("수정 제목")
                .content("수정 내용")
                .build();
        given(inquiryService.updateInquiry(any(), eq(1L), any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/inquiries/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("수정 제목"));
    }

    @Test
    @DisplayName("문의 삭제 성공")
    @WithMockUserPrincipal
    void deleteInquiry_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/inquiries/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

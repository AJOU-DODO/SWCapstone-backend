package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NestPostController.class)
@Import(SecurityConfig.class)
class NestPostControllerTest {

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
    @DisplayName("둥지 생성 성공")
    @WithMockUser
    void createNest_success() throws Exception {
        NestCreateRequestDto requestDto = new NestCreateRequestDto("새둥지", "내용", 37.5, 127.0, 100, List.of(1L), List.of("url"), false);
        NestSummaryResponseDto responseDto = NestSummaryResponseDto.builder().id(1L).title("새둥지").build();

        given(nestService.createNest(any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/nests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("새둥지"));
    }

    @Test
    @DisplayName("둥지 상세 조회 성공")
    @WithMockUser
    void getNestDetail_success() throws Exception {
        Long nestId = 1L;
        NestDetailResponseDto responseDto = NestDetailResponseDto.builder().id(nestId).title("상세조회").build();

        given(nestService.getNestDetail(any(), eq(nestId))).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/nests/{id}", nestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("상세조회"));
    }

    @Test
    @DisplayName("둥지 수정 성공")
    @WithMockUser
    void updateNest_success() throws Exception {
        Long nestId = 1L;
        NestUpdateRequestDto requestDto = new NestUpdateRequestDto("수정제목", null, null, null, null);
        NestSummaryResponseDto responseDto = NestSummaryResponseDto.builder().id(nestId).title("수정제목").build();

        given(nestService.updateNest(any(), eq(nestId), any())).willReturn(responseDto);

        mockMvc.perform(patch("/api/v1/nests/{id}", nestId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정제목"));
    }

    @Test
    @DisplayName("둥지 삭제 성공")
    @WithMockUser
    void deleteNest_success() throws Exception {
        Long nestId = 1L;

        mockMvc.perform(delete("/api/v1/nests/{id}", nestId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("둥지가 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("댓글 작성 성공")
    @WithMockUser
    void createComment_success() throws Exception {
        Long nestId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("댓글내용", null);

        mockMvc.perform(post("/api/v1/nests/{id}/comments", nestId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("댓글이 성공적으로 작성되었습니다."));
    }

    @Test
    @DisplayName("댓글 수정 성공")
    @WithMockUser
    void updateComment_success() throws Exception {
        Long commentId = 10L;
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정내용");

        mockMvc.perform(patch("/api/v1/nests/comments/{commentId}", commentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("댓글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @WithMockUser
    void deleteComment_success() throws Exception {
        Long commentId = 10L;

        mockMvc.perform(delete("/api/v1/nests/comments/{commentId}", commentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("댓글이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("리액션 처리 성공")
    @WithMockUser
    void handleReaction_success() throws Exception {
        Long nestId = 1L;

        mockMvc.perform(post("/api/v1/nests/{id}/reaction", nestId)
                        .with(csrf())
                        .param("type", "LIKE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("리액션이 성공적으로 처리되었습니다."));
    }
}

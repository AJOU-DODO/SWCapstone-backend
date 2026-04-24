package com.dodo.dodoserver.domain.nest.controller;

import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.service.NestDraftService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NestDraftController.class)
@Import(SecurityConfig.class)
class NestDraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NestDraftService nestDraftService;

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
    @DisplayName("임시 저장 생성 성공")
    @WithMockUserPrincipal
    void createDraft_success() throws Exception {
        NestDraftCreateRequestDto requestDto = new NestDraftCreateRequestDto(37.5, 127.0, "임시제목", "임시내용", 100, List.of(1L), List.of("url"));
        NestDraftResponseDto responseDto = NestDraftResponseDto.builder().id(1L).title("임시제목").build();

        given(nestDraftService.createDraft(any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/nests/drafts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("임시제목"));
    }

    @Test
    @DisplayName("내 임시 저장 목록 조회 성공")
    @WithMockUserPrincipal
    void getMyDrafts_success() throws Exception {
        NestDraftResponseDto responseDto = NestDraftResponseDto.builder().id(1L).title("임시목록").build();
        given(nestDraftService.getMyDrafts(any())).willReturn(List.of(responseDto));

        mockMvc.perform(get("/api/v1/nests/drafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("임시목록"));
    }

    @Test
    @DisplayName("임시 저장 상세 조회 성공")
    @WithMockUserPrincipal
    void getDraftDetail_success() throws Exception {
        Long draftId = 1L;
        NestDraftResponseDto responseDto = NestDraftResponseDto.builder().id(draftId).title("상세조회").build();

        given(nestDraftService.getDraftDetail(any(), eq(draftId))).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/nests/drafts/{id}", draftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("상세조회"));
    }

    @Test
    @DisplayName("임시 저장 수정 성공")
    @WithMockUserPrincipal
    void updateDraft_success() throws Exception {
        Long draftId = 1L;
        NestDraftUpdateRequestDto requestDto = new NestDraftUpdateRequestDto("수정제목", null, null, null, null);
        NestDraftResponseDto responseDto = NestDraftResponseDto.builder().id(draftId).title("수정제목").build();

        given(nestDraftService.updateDraft(any(), eq(draftId), any())).willReturn(responseDto);

        mockMvc.perform(patch("/api/v1/nests/drafts/{id}", draftId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정제목"));
    }

    @Test
    @DisplayName("임시 저장 삭제 성공")
    @WithMockUserPrincipal
    void deleteDraft_success() throws Exception {
        Long draftId = 1L;

        mockMvc.perform(delete("/api/v1/nests/drafts/{id}", draftId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("임시 저장 발행 성공")
    @WithMockUserPrincipal
    void publishDraft_success() throws Exception {
        Long draftId = 1L;
        NestSimpleResponseDto responseDto = NestSimpleResponseDto.builder().id(100L).build();

        given(nestDraftService.publishDraft(any(), eq(draftId))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/nests/drafts/{id}/publish", draftId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100L));
    }
}

package com.dodo.dodoserver.domain.postcard.controller;

import com.dodo.dodoserver.domain.postcard.dto.*;
import com.dodo.dodoserver.domain.postcard.service.PostcardService;
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
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(PostcardController.class)
@Import(SecurityConfig.class)
class PostcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostcardService postcardService;

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
    @DisplayName("엽서 등록 성공")
    @WithMockUserPrincipal(id = 1L)
    void createPostcard_success() throws Exception {
        // given
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("http://image.url", "엽서 내용");
        PostcardResponseDto responseDto = PostcardResponseDto.builder()
                .id(1L)
                .content("엽서 내용")
                .imageUrl("http://image.url")
                .build();
        given(postcardService.createPostcard(eq(1L), any(PostcardCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/postcards")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").value("엽서 내용"));
    }

    @Test
    @DisplayName("엽서 상세 조회 성공")
    @WithMockUserPrincipal(id = 1L)
    void getPostcardDetail_success() throws Exception {
        // given
        PostcardResponseDto responseDto = PostcardResponseDto.builder()
                .id(1L)
                .content("상세 내용")
                .build();
        given(postcardService.getPostcardDetail(1L, 1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/postcards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("상세 내용"));
    }

    @Test
    @DisplayName("엽서 수정 성공")
    @WithMockUserPrincipal(id = 1L)
    void updatePostcard_success() throws Exception {
        // given
        PostcardCreateRequestDto requestDto = new PostcardCreateRequestDto("http://new.url", "수정된 내용");
        PostcardResponseDto responseDto = PostcardResponseDto.builder()
                .id(1L)
                .content("수정된 내용")
                .imageUrl("http://new.url")
                .build();
        given(postcardService.updatePostcard(eq(1L), eq(1L), any(PostcardCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/postcards/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("엽서 삭제 성공")
    @WithMockUserPrincipal(id = 1L)
    void deletePostcard_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/postcards/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("엽서 인벤토리 조회 성공")
    @WithMockUserPrincipal(id = 1L)
    void getPostcardInventory_success() throws Exception {
        // given
        PostcardResponseDto responseDto = PostcardResponseDto.builder()
                .id(1L)
                .content("인벤토리 엽서")
                .build();
        Page<PostcardResponseDto> page = new PageImpl<>(List.of(responseDto));
        given(postcardService.getPostcardInventory(eq(1L), eq("ALL"), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/postcards/inventory")
                        .param("filter", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].content").value("인벤토리 엽서"));
    }

    @Test
    @DisplayName("엽서 교환 가능 여부 확인 성공")
    @WithMockUserPrincipal(id = 1L)
    void checkExchangeAvailability_success() throws Exception {
        // given
        PostcardExchangeCheckResponseDto responseDto = PostcardExchangeCheckResponseDto.builder()
                .canExchange(true)
                .remainingCount(3)
                .build();
        given(postcardService.checkExchangeAvailability(1L)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/postcards/exchange-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.canExchange").value(true))
                .andExpect(jsonPath("$.data.remainingCount").value(3));
    }

    @Test
    @DisplayName("엽서 교환 성공")
    @WithMockUserPrincipal(id = 1L)
    void exchangePostcard_success() throws Exception {
        // given
        PostcardExchangeRequestDto requestDto = new PostcardExchangeRequestDto(10L);
        PostcardResponseDto responseDto = PostcardResponseDto.builder()
                .id(20L)
                .content("교환된 엽서")
                .build();
        given(postcardService.exchangePostcard(eq(1L), eq(1L), any(PostcardExchangeRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/nests/1/exchange")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").value("교환된 엽서"));
    }

    @Test
    @DisplayName("엽서 리액션 반응 성공")
    @WithMockUserPrincipal(id = 1L)
    void addReaction_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/postcards/1/reactions")
                        .with(csrf())
                        .param("type", "BEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}

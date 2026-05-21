package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.domain.admin.user.dao.SanctionHistoryRepository;
import com.dodo.dodoserver.domain.admin.user.entity.SanctionHistory;
import com.dodo.dodoserver.domain.auth.service.AuthService;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SanctionHistoryRepository sanctionHistoryRepository;

    @Mock
    private RedirectStrategy redirectStrategy;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private User user;
    private UserPrincipal principal;
    private final String AUTHORIZED_REDIRECT_URI = "http://localhost:3000/admin/login/callback";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .role(Role.ADMIN)
                .build();

        principal = UserPrincipal.create(1L, "test@example.com", "ROLE_ADMIN");

        // ReflectionTestUtils를 사용하여 @Value 필드 주입
        ReflectionTestUtils.setField(successHandler, "authorizedRedirectUris", List.of(AUTHORIZED_REDIRECT_URI));
        
        // RedirectStrategy 설정 (getRedirectStrategy().sendRedirect() 호출 대응)
        successHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("앱 요청 시 (쿠키 없음) JSON 응답을 반환한다")
    void onAuthenticationSuccess_AppRequest_ReturnsJson() throws IOException {
        // given
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(request.getCookies()).thenReturn(null);
        when(tokenProvider.createAccessToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
        when(tokenProvider.createRefreshToken(anyString())).thenReturn("refresh-token");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        String body = stringWriter.toString();
        assertTrue(body.contains("access-token"));
        assertTrue(body.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("관리자 웹 로그인 성공 시 리다이렉트한다")
    void onAuthenticationSuccess_WebAdminRequest_RedirectsToTargetUrl() throws IOException {
        // given
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Cookie redirectCookie = new Cookie("redirect_uri", AUTHORIZED_REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectCookie});
        
        when(tokenProvider.createAccessToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
        when(tokenProvider.createRefreshToken(anyString())).thenReturn("refresh-token");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), captor.capture());
        String url = captor.getValue();
        assertTrue(url.contains("status=SUCCESS"));
        assertTrue(url.contains("accessToken=access-token"));
        assertTrue(url.contains("role=ROLE_ADMIN"));
        
        // 쿠키 삭제 확인
        verify(response).addCookie(argThat(cookie -> 
            "redirect_uri".equals(cookie.getName()) && cookie.getMaxAge() == 0
        ));
    }

    @Test
    @DisplayName("일반 유저가 웹 로그인 시도 시 권한 에러와 함께 리다이렉트한다")
    void onAuthenticationSuccess_WebUserRequest_RedirectsWithError() throws IOException {
        // given
        UserPrincipal userPrincipal = UserPrincipal.create(1L, "user@example.com", "ROLE_USER");
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Cookie redirectCookie = new Cookie("redirect_uri", AUTHORIZED_REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectCookie});

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), captor.capture());
        String url = captor.getValue();
        assertTrue(url.contains("status=ERROR"));
        assertTrue(url.contains("code=" + ErrorCode.HANDLE_ACCESS_DENIED.getCode()));
    }

    @Test
    @DisplayName("제재된 유저가 웹 로그인 시도 시 제재 정보와 함께 리다이렉트한다")
    void onAuthenticationSuccess_WebSanctinedUserRequest_RedirectsWithSanctionInfo() throws IOException {
        // given
        user.setSanctionedUntil(LocalDateTime.now().plusDays(7));
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Cookie redirectCookie = new Cookie("redirect_uri", AUTHORIZED_REDIRECT_URI);
        when(request.getCookies()).thenReturn(new Cookie[]{redirectCookie});

        SanctionHistory history = SanctionHistory.builder()
                .reason("부적절한 게시글 작성")
                .build();
        when(sanctionHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(history));

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), captor.capture());
        String url = captor.getValue();
        assertTrue(url.contains("status=ERROR"));
        assertTrue(url.contains("code=" + ErrorCode.USER_SANCTIONED.getCode()));
        assertTrue(url.contains("reason="));
    }

    @Test
    @DisplayName("허용되지 않은 리다이렉트 URI 쿠키 발견 시 403 에러를 반환한다")
    void onAuthenticationSuccess_WebUnauthorizedRedirectUri_ReturnsForbidden() throws IOException {
        // given
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Cookie redirectCookie = new Cookie("redirect_uri", "http://malicious-site.com");
        when(request.getCookies()).thenReturn(new Cookie[]{redirectCookie});

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(redirectStrategy, never()).sendRedirect(any(), any(), anyString());
    }
}

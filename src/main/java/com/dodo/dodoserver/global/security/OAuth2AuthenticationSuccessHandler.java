package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.dto.ApiResponseDto;
import com.dodo.dodoserver.dto.TokenResponseDto;
import com.dodo.dodoserver.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인(OAuth2)이 최종 성공했을 때 실행되는 핸들러입니다.
 * JWT 토큰을 발급하고 클라이언트를 특정 URL로 리다이렉트시킵니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final ObjectMapper objectMapper; // JSON 변환용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 인증된 사용자 정보를 가져
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        // Access Token과 Refresh Token을 생성
        String accessToken = tokenProvider.createAccessToken(email, role);
        String refreshToken = tokenProvider.createRefreshToken(email);

        // 보안을 위해 Refresh Token을 Redis에 저장
        authService.saveRefreshToken(email, refreshToken);

        // 리다이렉트 대신 JSON Body 응답
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);


        String result = objectMapper.writeValueAsString(ApiResponseDto.success(
            TokenResponseDto.of(accessToken, refreshToken, 1800L)
        ));

        response.getWriter().write(result);

        // String targetUrl = UriComponentsBuilder.fromUriString("/login/success")
        //         .queryParam("accessToken", accessToken)
        //         .queryParam("refreshToken", refreshToken)
        //         .build().toUriString();
        //
        // // 5. 지정된 URL로 리다이렉트 실행
        // getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

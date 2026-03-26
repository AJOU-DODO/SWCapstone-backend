package com.dodo.dodoserver.security;

import com.dodo.dodoserver.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 인증된 사용자 정보를 가져옵니다.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        // 2. Access Token과 Refresh Token을 생성합니다.
        String accessToken = tokenProvider.createAccessToken(email, role);
        String refreshToken = tokenProvider.createRefreshToken(email);

        // 3. 보안을 위해 Refresh Token을 Redis에 저장합니다 (만료 시 재발급에 사용).
        authService.saveRefreshToken(email, refreshToken);

        // 4. 클라이언트(프론트엔드/앱)로 토큰을 전달하기 위해 리다이렉트 URL을 구성합니다.
        // 실무에서는 보안을 위해 쿠키에 담거나 별도의 처리를 할 수 있지만, 여기서는 쿼리 파라미터로 전달합니다.
        String targetUrl = UriComponentsBuilder.fromUriString("/login/success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 5. 지정된 URL로 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

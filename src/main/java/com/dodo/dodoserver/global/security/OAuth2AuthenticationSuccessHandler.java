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

/*
  소셜 로그인(OAuth2)이 최종 성공했을 때 실행되는 핸들러
 */
import com.dodo.dodoserver.entity.User;
import com.dodo.dodoserver.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository; // 추가: 유저 조회를 위함
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        String accessToken = tokenProvider.createAccessToken(email, role);
        String refreshToken = tokenProvider.createRefreshToken(email);
        authService.saveRefreshToken(email, refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // TokenResponseDto 생성 시 isOnboarded 값을 함께 전달
        String result = objectMapper.writeValueAsString(ApiResponseDto.success(
            TokenResponseDto.of(accessToken, refreshToken, 1800L, user.isOnboarded())
        ));

        response.getWriter().write(result);
    }
}

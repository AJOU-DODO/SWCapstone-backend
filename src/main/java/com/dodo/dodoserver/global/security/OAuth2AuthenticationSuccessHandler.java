package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.domain.auth.service.AuthService;
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
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.dao.UserRepository;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository; // 추가: 유저 조회를 위함
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        String role = principal.getRole();

        String accessToken = tokenProvider.createAccessToken(user.getId(), email, role);
        String refreshToken = tokenProvider.createRefreshToken(email);
        authService.saveRefreshToken(user.getId(), refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // TokenResponseDto 생성 시 isOnboarded 값을 함께 전달
        String result = objectMapper.writeValueAsString(ApiResponseDto.success(
            TokenResponseDto.of(accessToken, refreshToken, 1800L, user.isOnboarded())
        ));

        response.getWriter().write(result);
    }
}

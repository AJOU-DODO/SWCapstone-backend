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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/*
  소셜 로그인(OAuth2)이 최종 성공했을 때 실행되는 핸들러
 */
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.admin.user.dao.SanctionHistoryRepository;
import com.dodo.dodoserver.domain.admin.user.entity.SanctionHistory;
import com.dodo.dodoserver.domain.admin.user.dto.UserSanctionErrorResponseDto;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final UserRepository userRepository; // 추가: 유저 조회를 위함
    private final SanctionHistoryRepository sanctionHistoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 제재 여부 확인
        if (isSanctioned(user)) {
            sendSanctionResponse(response, user);
            return;
        }
        
        String role = principal.getRole();

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail());
        authService.saveRefreshToken(user.getId(), refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // TokenResponseDto 생성 시 isOnboarded 값을 함께 전달
        String result = objectMapper.writeValueAsString(ApiResponseDto.success(
            TokenResponseDto.of(accessToken, refreshToken, 1800L, user.isOnboarded(), role)
        ));

        response.getWriter().write(result);
    }

    private boolean isSanctioned(User user) {
        return user.getSanctionedUntil() != null && 
               user.getSanctionedUntil().isAfter(LocalDateTime.now());
    }

    private void sendSanctionResponse(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(ErrorCode.USER_SANCTIONED.getStatus().value());

        // 가장 최근의 제재 사유 조회
        String reason = sanctionHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .findFirst()
                .map(SanctionHistory::getReason)
                .orElse("사유가 등록되지 않았습니다.");

        ApiResponseDto<UserSanctionErrorResponseDto> errorResponse = ApiResponseDto.error(
            ErrorCode.USER_SANCTIONED.getCode(),
            ErrorCode.USER_SANCTIONED.getMessage(),
            UserSanctionErrorResponseDto.builder()
                .sanctionedUntil(user.getSanctionedUntil())
                .reason(reason)
                .build()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

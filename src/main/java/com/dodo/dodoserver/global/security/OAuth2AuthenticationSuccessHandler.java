package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Value("${app.oauth2.authorized-redirect-uris}")
    private List<String> authorizedRedirectUris;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    /**
     * OAuth2 로그인이 성공했을 때 호출되는 메인 핸들러 메서드
     * 쿠키 존재 여부에 따라 웹(리다이렉트)과 앱(JSON) 응답을 분기합니다.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Optional<String> redirectUri = getRedirectUriFromCookie(request);

        // 웹 로그인 시도 시 관리자 권한 확인
        if (redirectUri.isPresent() && !"ROLE_ADMIN".equals(principal.getRole())) {
            handleWebErrorResponse(request, response, ErrorCode.HANDLE_ACCESS_DENIED, redirectUri.get());
            return;
        }

        // 제재된 사용자인 경우 각 환경에 맞는 에러 응답 반환
        if (isSanctioned(user)) {
            if (redirectUri.isPresent()) {
                handleWebSanctionResponse(request, response, user, redirectUri.get());
            } else {
                sendSanctionResponse(response, user);
            }
            return;
        }
        
        String role = principal.getRole();

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail());
        authService.saveRefreshToken(user.getId(), refreshToken);

        // 웹 요청(쿠키 있음)과 앱 요청(쿠키 없음)에 따른 성공 응답 처리
        if (redirectUri.isPresent()) {
            handleWebSuccessResponse(request, response, accessToken, refreshToken, user, role, redirectUri.get());
        } else {
            sendAppSuccessResponse(response, accessToken, refreshToken, user, role);
        }
    }

    /**
     * 클라이언트(웹)가 보낸 리다이렉트 대상 주소 쿠키를 추출합니다.
     */
    private Optional<String> getRedirectUriFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REDIRECT_URI_PARAM_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .map(value -> URLDecoder.decode(value, StandardCharsets.UTF_8));
    }

    /**
     * 웹 관리자 페이지를 위한 성공 응답 처리: 쿼리 파라미터에 토큰을 실어 리다이렉트합니다.
     */
    private void handleWebSuccessResponse(HttpServletRequest request, HttpServletResponse response, String accessToken, String refreshToken, User user, String role, String targetUrl) throws IOException {
        if (!isAuthorizedRedirectUri(targetUrl)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized redirect URI");
            return;
        }

        String url = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("status", "SUCCESS")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("onboarded", user.isOnboarded())
                .queryParam("role", role)
                .encode()
                .build().toUriString();

        clearRedirectCookie(request, response);
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    /**
     * 웹 관리자 페이지를 위한 일반 에러 응답 처리: 쿼리 파라미터에 에러 정보를 실어 리다이렉트합니다.
     */
    private void handleWebErrorResponse(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode, String targetUrl) throws IOException {
        if (!isAuthorizedRedirectUri(targetUrl)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized redirect URI");
            return;
        }

        String url = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("status", "ERROR")
                .queryParam("code", errorCode.getCode())
                .queryParam("reason", errorCode.getMessage())
                .encode()
                .build().toUriString();

        clearRedirectCookie(request, response);
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    /**
     * 안드로이드 앱을 위한 성공 응답 처리: 기존 규격대로 JSON 데이터를 반환합니다.
     */
    private void sendAppSuccessResponse(HttpServletResponse response, String accessToken, String refreshToken, User user, String role) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String result = objectMapper.writeValueAsString(ApiResponseDto.success(
                TokenResponseDto.of(accessToken, refreshToken, 1800L, user.isOnboarded(), role)
        ));

        response.getWriter().write(result);
    }

    /**
     * 사용자가 현재 제재 상태인지 확인합니다.
     */
    private boolean isSanctioned(User user) {
        return user.getSanctionedUntil() != null && 
               user.getSanctionedUntil().isAfter(LocalDateTime.now());
    }

    /**
     * 웹 관리자 페이지를 위한 제재 에러 응답 처리: 쿼리 파라미터에 제재 정보를 실어 리다이렉트합니다.
     */
    private void handleWebSanctionResponse(HttpServletRequest request, HttpServletResponse response, User user, String targetUrl) throws IOException {
        if (!isAuthorizedRedirectUri(targetUrl)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized redirect URI");
            return;
        }

        String reason = getLatestSanctionReason(user);
        String url = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("status", "ERROR")
                .queryParam("code", ErrorCode.USER_SANCTIONED.getCode())
                .queryParam("reason", reason)
                .queryParam("sanctionedUntil", user.getSanctionedUntil().toString())
                .encode()
                .build().toUriString();

        clearRedirectCookie(request, response);
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    /**
     * 안드로이드 앱을 위한 제재 에러 응답 처리: 기존 규격대로 JSON 에러 데이터를 반환합니다.
     */
    private void sendSanctionResponse(HttpServletResponse response, User user) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(ErrorCode.USER_SANCTIONED.getStatus().value());

        String reason = getLatestSanctionReason(user);

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

    /**
     * 사용자의 가장 최근 제재 사유를 조회합니다.
     */
    private String getLatestSanctionReason(User user) {
        return sanctionHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .findFirst()
                .map(SanctionHistory::getReason)
                .orElse("사유가 등록되지 않았습니다.");
    }

    /**
     * 사용이 끝난 리다이렉트용 쿠키를 브라우저에서 삭제합니다.
     */
    private void clearRedirectCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 요청된 URI가 허용된 패턴 중 하나와 일치하는지 확인합니다. (Open Redirect 및 토큰 탈취 방어)
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        return authorizedRedirectUris.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}

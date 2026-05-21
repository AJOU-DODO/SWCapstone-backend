package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.global.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 인증 시작 시 쿼리 파라미터로 전달된 redirect_uri를 쿠키에 저장하는 필터입니다.
 * AntPathMatcher를 사용하여 와일드카드가 포함된 패턴 기반 검증을 지원합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2RedirectUriFilter extends OncePerRequestFilter {

    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String AUTHORIZATION_ENDPOINT_PREFIX = "/oauth2/authorization/";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. OAuth2 인증 시작 요청인지 확인
        if (request.getRequestURI().startsWith(AUTHORIZATION_ENDPOINT_PREFIX)) {
            String redirectUri = request.getParameter(REDIRECT_URI_PARAM);
            
            // 2. redirect_uri 파라미터가 존재하면 패턴 검증 후 쿠키에 저장
            if (redirectUri != null && !redirectUri.isBlank()) {
                if (isAuthorizedRedirectUri(redirectUri)) {
                    Cookie cookie = new Cookie(REDIRECT_URI_PARAM, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(300);
                    
                    response.addCookie(cookie);
                } else {
                    log.warn("Unauthorized redirect_uri attempted: {}", redirectUri);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * AntPathMatcher를 사용하여 요청된 URI가 허용된 패턴 중 하나와 일치하는지 확인합니다.
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        return appProperties.getOauth2().getAuthorizedRedirectUris().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}

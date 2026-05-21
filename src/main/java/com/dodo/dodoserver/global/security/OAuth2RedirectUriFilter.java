package com.dodo.dodoserver.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 인증 시작 시 쿼리 파라미터로 전달된 redirect_uri를 쿠키에 저장하는 필터입니다.
 * 이를 통해 도메인이 다른 프론트엔드 환경에서도 동적 리다이렉트가 가능해집니다.
 */
@Component
public class OAuth2RedirectUriFilter extends OncePerRequestFilter {

    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String AUTHORIZATION_ENDPOINT_PREFIX = "/oauth2/authorization/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. OAuth2 인증 시작 요청인지 확인 (예: /oauth2/authorization/google)
        if (request.getRequestURI().startsWith(AUTHORIZATION_ENDPOINT_PREFIX)) {
            String redirectUri = request.getParameter(REDIRECT_URI_PARAM);
            
            // 2. redirect_uri 파라미터가 존재하면 쿠키에 저장
            if (redirectUri != null && !redirectUri.isBlank()) {
                Cookie cookie = new Cookie(REDIRECT_URI_PARAM, URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(300); // 5분간 유효
                
                // 도메인이 다른 웹 환경에서 전달된 파라미터를 백엔드 도메인의 쿠키로 굽습니다.
                response.addCookie(cookie);
            }
        }

        filterChain.doFilter(request, response);
    }
}

package com.dodo.dodoserver.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 API 요청에 대해 메서드, URL, 상태 코드, 처리 시간을 로그로 남기는 필터
 */
@Slf4j
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        // [GET] /api/hello (200) - 12ms 형식으로 출력
        log.info(">>> [API LOG] [{}] {} ({}) - {}ms", method, uri, status, duration);
    }
}

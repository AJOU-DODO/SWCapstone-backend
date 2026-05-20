package com.dodo.dodoserver.global.config;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class SoftDeleteFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.dodo.dodoserver.domain..service..*.*(..))")
    public void applySoftDeleteFilter() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String requestURI = request.getRequestURI();
        Session session = entityManager.unwrap(Session.class);

        // 1. 관리자 API는 모든 필터 비활성화 (모든 데이터 조회)
        if (requestURI.startsWith("/api/v1/admin")) {
            session.disableFilter("nestFilter");
            session.disableFilter("commentFilter");
            session.disableFilter("postcardFilter");
            return;
        }

        // 2. 둥지 댓글 조회 API 예외 처리
        // 둥지는 살아있어야 하므로 nestFilter는 유지, 댓글 마스킹을 위해 commentFilter만 비활성화
        if (requestURI.matches("^/api/v1/nests/\\d+/comments.*")) {
            session.enableFilter("nestFilter");
            session.disableFilter("commentFilter");
            session.enableFilter("postcardFilter");
            return;
        }

        // 3. 그 외 모든 일반 API는 모든 필터 활성화 (소프트 삭제 데이터 숨김)
        session.enableFilter("nestFilter");
        session.enableFilter("commentFilter");
        session.enableFilter("postcardFilter");
    }
}

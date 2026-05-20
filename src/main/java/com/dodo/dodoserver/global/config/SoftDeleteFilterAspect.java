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

        // 1. 관리자 API는 필터 비활성화 (모든 데이터 조회)
        if (requestURI.startsWith("/api/v1/admin")) {
            session.disableFilter("deletedFilter");
            return;
        }

        // 2. 둥지 댓글 조회 API는 필터 비활성화 (삭제된 댓글 마스킹 노출을 위해)
        // 매칭 패턴: /api/v1/nests/{id}/comments
        if (requestURI.matches("^/api/v1/nests/\\d+/comments.*")) {
            session.disableFilter("deletedFilter");
            return;
        }

        // 3. 그 외 모든 일반 API는 필터 활성화 (소프트 삭제 데이터 숨김)
        session.enableFilter("deletedFilter");
    }
}

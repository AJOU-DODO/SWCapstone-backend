package com.dodo.dodoserver.domain.admin.inquiry.dao;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dodo.dodoserver.domain.inquiry.entity.QInquiry.inquiry;
import static com.dodo.dodoserver.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminInquiryRepositoryCustomImpl implements AdminInquiryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminInquiryResponseDto> findInquiriesForAdmin(InquiryStatus status, Pageable pageable) {
        List<AdminInquiryResponseDto> content = queryFactory
                .select(Projections.constructor(AdminInquiryResponseDto.class,
                        inquiry.id,
                        user.id,
                        user.nickname,
                        inquiry.type,
                        inquiry.type.stringValue(), // Enum의 description 대신 stringValue() 사용 후 DTO에서 처리하거나 Projections 조정 필요
                        inquiry.title,
                        inquiry.status,
                        inquiry.status.stringValue(),
                        inquiry.createdAt,
                        inquiry.answeredAt
                ))
                .from(inquiry)
                .join(inquiry.user, user)
                .where(statusEq(status))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(inquiry.createdAt.desc())
                .fetch();

        // Projections.constructor는 필드 순서와 타입을 맞춰야 함. 
        // AdminInquiryResponseDto를 Projections.constructor에 맞게 수정하거나 직접 맵핑 권장.
        // 여기서는 QBean이나 직접 fetch 후 mapping 방식을 사용하겠음.

        List<AdminInquiryResponseDto> mappedContent = queryFactory
                .selectFrom(inquiry)
                .join(inquiry.user, user).fetchJoin()
                .where(statusEq(status))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(inquiry.createdAt.desc())
                .fetch()
                .stream()
                .map(AdminInquiryResponseDto::from)
                .toList();

        JPAQuery<Long> countQuery = queryFactory
                .select(inquiry.count())
                .from(inquiry)
                .where(statusEq(status));

        return PageableExecutionUtils.getPage(mappedContent, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(InquiryStatus status) {
        return status != null ? inquiry.status.eq(status) : null;
    }
}

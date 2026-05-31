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

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(InquiryStatus status) {
        return status != null ? inquiry.status.eq(status) : null;
    }
}

package com.dodo.dodoserver.domain.admin.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.ad.entity.QNestAdInfo;
import com.dodo.dodoserver.domain.admin.ad.dto.AdStatusFilter;
import com.dodo.dodoserver.domain.nest.entity.QNest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdAdminRepositoryImpl implements AdAdminRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NestAdInfo> findAllWithFilter(AdStatusFilter filter, Pageable pageable) {
        QNestAdInfo nestAdInfo = QNestAdInfo.nestAdInfo;
        QNest nest = QNest.nest;

        List<NestAdInfo> content = queryFactory
                .selectFrom(nestAdInfo)
                .join(nestAdInfo.nest, nest).fetchJoin()
                .where(statusEq(filter))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(nest.createdAt.desc())
                .fetch();

        JPAQuery<Long> totalQuery = queryFactory
                .select(Wildcard.count)
                .from(nestAdInfo)
                .join(nestAdInfo.nest, nest)
                .where(statusEq(filter));

        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    private BooleanExpression statusEq(AdStatusFilter filter) {
        if (filter == null || filter == AdStatusFilter.ALL) {
            return null;
        }
        if (filter == AdStatusFilter.ACTIVE) {
            return QNest.nest.deletedAt.isNull();
        }
        if (filter == AdStatusFilter.DELETED) {
            return QNest.nest.deletedAt.isNotNull();
        }
        return null;
    }
}

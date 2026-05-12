package com.dodo.dodoserver.domain.admin.user.dao;

import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.nest.entity.QNest;
import com.dodo.dodoserver.domain.nest.entity.QNestComment;
import com.dodo.dodoserver.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
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
public class UserAdminRepositoryImpl implements UserAdminRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserAdminResponseDto> findAllUserAdminInfo(Pageable pageable) {
        QUser user = QUser.user;
        QNest nest = QNest.nest;
        QNestComment comment = QNestComment.nestComment;

        List<UserAdminResponseDto> content = queryFactory
                .select(Projections.constructor(UserAdminResponseDto.class,
                        user.id,
                        user.nickname,
                        user.email,
                        user.role,
                        user.createdAt,
                        nest.id.countDistinct(),
                        comment.id.countDistinct(),
                        user.sanctionedUntil
                ))
                .from(user)
                .leftJoin(nest).on(nest.creator.eq(user))
                .leftJoin(comment).on(comment.user.eq(user))
                .groupBy(user.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> totalQuery = queryFactory
                .select(Wildcard.count)
                .from(user);

        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }
}

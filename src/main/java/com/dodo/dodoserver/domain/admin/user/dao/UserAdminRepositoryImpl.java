package com.dodo.dodoserver.domain.admin.user.dao;

import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.nest.entity.QNest;
import com.dodo.dodoserver.domain.nest.entity.QNestComment;
import com.dodo.dodoserver.domain.user.entity.QUser;
import com.dodo.dodoserver.domain.user.entity.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> totalQuery = queryFactory
                .select(Wildcard.count)
                .from(user);

        return PageableExecutionUtils.getPage(content, pageable, totalQuery::fetchOne);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        
        if (sort.isEmpty()) {
            specifiers.add(new OrderSpecifier<>(Order.DESC, QUser.user.createdAt));
            return specifiers.toArray(new OrderSpecifier[0]);
        }

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            if ("nestCount".equals(property)) {
                specifiers.add(new OrderSpecifier<>(direction, QNest.nest.id.countDistinct()));
                continue;
            }
            if ("commentCount".equals(property)) {
                specifiers.add(new OrderSpecifier<>(direction, QNestComment.nestComment.id.countDistinct()));
                continue;
            }

            PathBuilder<User> pathBuilder = new PathBuilder<>(User.class, "user");
            specifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
        }
        
        return specifiers.toArray(new OrderSpecifier[0]);
    }
}

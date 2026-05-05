package com.dodo.dodoserver.domain.mypage.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.user.entity.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dodo.dodoserver.domain.nest.entity.QNest.nest;
import static com.dodo.dodoserver.domain.nest.entity.QNestCategory.nestCategory;
import static com.dodo.dodoserver.domain.nest.entity.QNestComment.nestComment;
import static com.dodo.dodoserver.domain.nest.entity.QNestDraft.nestDraft;
import static com.dodo.dodoserver.domain.nest.entity.QNestReaction.nestReaction;
import static com.dodo.dodoserver.domain.nest.entity.QUnlockHistory.unlockHistory;
import static com.dodo.dodoserver.domain.postcard.entity.QPostcard.postcard;

@Repository
@RequiredArgsConstructor
public class MyPageRepositoryImpl implements MyPageRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Nest> findNestsByUserAndCategory(User user, Long categoryId, Pageable pageable) {
        JPAQuery<Nest> query = queryFactory
                .selectFrom(nest)
                .where(
                        nest.creator.eq(user),
                        nest.deletedAt.isNull(),
                        categoryIdEq(categoryId)
                );

        if (categoryId != null) {
            query.join(nestCategory).on(nestCategory.nest.eq(nest))
                 .where(nestCategory.category.id.eq(categoryId));
        }

        long total = query.fetchCount();

        List<Nest> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), Nest.class, "nest"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<NestComment> findCommentsByUser(User user, Pageable pageable) {
        JPAQuery<NestComment> query = queryFactory
                .selectFrom(nestComment)
                .where(
                        nestComment.user.eq(user),
                        nestComment.deletedAt.isNull()
                );

        long total = query.fetchCount();

        List<NestComment> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), NestComment.class, "nestComment"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<NestDraft> findDraftsByUser(User user, Pageable pageable) {
        JPAQuery<NestDraft> query = queryFactory
                .selectFrom(nestDraft)
                .where(nestDraft.creator.eq(user));

        long total = query.fetchCount();

        List<NestDraft> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), NestDraft.class, "nestDraft"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Nest> findUnlockedNestsByUser(User user, Pageable pageable) {
        JPAQuery<Nest> query = queryFactory
                .select(nest)
                .from(unlockHistory)
                .join(unlockHistory.nest, nest)
                .where(
                        unlockHistory.user.eq(user),
                        nest.deletedAt.isNull()
                );

        long total = query.fetchCount();

        List<Nest> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), Nest.class, "nest"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<NestComment> findCommentsOnUserNests(User user, Pageable pageable) {
        JPAQuery<NestComment> query = queryFactory
                .selectFrom(nestComment)
                .join(nestComment.nest, nest)
                .where(
                        nest.creator.eq(user),
                        nestComment.user.ne(user), // Optional: exclude user's own comments on their own nests
                        nestComment.deletedAt.isNull()
                );

        long total = query.fetchCount();

        List<NestComment> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), NestComment.class, "nestComment"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Nest> findLikedNestsByUser(User user, Pageable pageable) {
        JPAQuery<Nest> query = queryFactory
                .select(nest)
                .from(nestReaction)
                .join(nestReaction.nest, nest)
                .where(
                        nestReaction.user.eq(user),
                        nestReaction.reactionType.eq(ReactionType.LIKE),
                        nest.deletedAt.isNull()
                );

        long total = query.fetchCount();

        List<Nest> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort(), Nest.class, "nest"))
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countNestsByUser(User user) {
        return queryFactory
                .selectFrom(nest)
                .where(nest.creator.eq(user), nest.deletedAt.isNull())
                .fetchCount();
    }

    @Override
    public long countCommentsByUser(User user) {
        return queryFactory
                .selectFrom(nestComment)
                .where(nestComment.user.eq(user), nestComment.deletedAt.isNull())
                .fetchCount();
    }

    @Override
    public long countPostcardsByUser(User user) {
        return queryFactory
                .selectFrom(postcard)
                .where(postcard.currentOwner.eq(user), postcard.deletedAt.isNull())
                .fetchCount();
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? nestCategory.category.id.eq(categoryId) : null;
    }

    private <T> OrderSpecifier<?>[] getOrderSpecifier(Sort sort, Class<T> clazz, String variable) {
        return sort.stream()
                .map(order -> {
                    Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                    PathBuilder<T> pathBuilder = new PathBuilder<>(clazz, variable);
                    return new OrderSpecifier(direction, pathBuilder.get(order.getProperty()));
                })
                .toArray(OrderSpecifier[]::new);
    }
}

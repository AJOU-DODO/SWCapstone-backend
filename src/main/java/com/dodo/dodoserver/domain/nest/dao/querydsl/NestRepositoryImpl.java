package com.dodo.dodoserver.domain.nest.dao.querydsl;

import com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto;
import com.dodo.dodoserver.domain.nest.entity.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.dodo.dodoserver.domain.nest.entity.QNest.nest;
import static com.dodo.dodoserver.domain.nest.entity.QNestLocation.nestLocation;
import static com.dodo.dodoserver.domain.nest.entity.QNestCategory.nestCategory;
import static com.dodo.dodoserver.domain.nest.entity.QNestReaction.nestReaction;

@RequiredArgsConstructor
public class NestRepositoryImpl implements NestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NestPinResponseDto> findNearbyPins(Point point, Double radiusMeter) {
        NumberTemplate<Double> distance = Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere({0}, {1})", nestLocation.point, point);

        return queryFactory
                .select(Projections.constructor(NestPinResponseDto.class,
                        nest.id,
                        Expressions.numberTemplate(Double.class, "ST_Latitude({0})", nestLocation.point),
                        Expressions.numberTemplate(Double.class, "ST_Longitude({0})", nestLocation.point)
                ))
                .from(nest)
                .join(nest.location, nestLocation)
                .where(distance.loe(radiusMeter), nest.deletedAt.isNull())
                .fetch();
    }

    @Override
    public Page<Nest> findNearbyNests(Point point, Double radiusMeter, Long categoryId, Pageable pageable) {
        NumberTemplate<Double> distance = Expressions.numberTemplate(Double.class,
                "ST_Distance_Sphere({0}, {1})", nestLocation.point, point);

        JPAQuery<Nest> query = queryFactory
                .selectFrom(nest)
                .join(nest.location, nestLocation)
                .leftJoin(nestCategory).on(nestCategory.nest.eq(nest))
                .leftJoin(nestReaction).on(nestReaction.nest.eq(nest))
                .where(
                        distance.loe(radiusMeter),
                        categoryEq(categoryId),
                        nest.deletedAt.isNull()
                )
                .groupBy(nest.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (OrderSpecifier<?> specifier : getOrderSpecifiers(pageable.getSort(), point)) {
            query.orderBy(specifier);
        }

        List<Nest> content = query.fetch();

        long total = queryFactory
                .select(nest.id.countDistinct())
                .from(nest)
                .join(nest.location, nestLocation)
                .leftJoin(nestCategory).on(nestCategory.nest.eq(nest))
                .where(
                        distance.loe(radiusMeter),
                        categoryEq(categoryId),
                        nest.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Double calculateDistance(Long nestId, Point point) {
        return queryFactory
                .select(Expressions.numberTemplate(Double.class,
                        "ST_Distance_Sphere({0}, {1})", nestLocation.point, point))
                .from(nestLocation)
                .where(nestLocation.nest.id.eq(nestId))
                .fetchOne();
    }

    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId != null ? nestCategory.category.id.eq(categoryId) : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort, Point point) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        if (sort.isUnsorted()) {
            specifiers.add(new OrderSpecifier<>(Order.DESC, nest.createdAt));
            return specifiers;
        }

        for (Sort.Order order : sort) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            switch (property) {
                case "createdAt", "created_at" -> specifiers.add(new OrderSpecifier<>(direction, nest.createdAt));
                case "viewCount", "view_count" -> specifiers.add(new OrderSpecifier<>(direction, nest.viewCount));
                case "distance" -> {
                    NumberTemplate<Double> distance = Expressions.numberTemplate(Double.class,
                            "ST_Distance_Sphere({0}, {1})", nestLocation.point, point);
                    specifiers.add(new OrderSpecifier<>(direction, distance));
                }
                case "like", "likeCount" -> {
                    NumberExpression<Long> likeCount = nestReaction.reactionType
                            .when(ReactionType.LIKE).then(1L)
                            .otherwise(0L).sum();
                    specifiers.add(new OrderSpecifier<>(direction, likeCount));
                }
                default -> specifiers.add(new OrderSpecifier<>(direction, Expressions.stringPath(nest, property)));
            }
        }

        return specifiers;
    }
}

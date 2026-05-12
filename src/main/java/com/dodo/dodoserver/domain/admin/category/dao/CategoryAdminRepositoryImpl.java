package com.dodo.dodoserver.domain.admin.category.dao;

import com.dodo.dodoserver.domain.admin.category.dto.AdminCategoryResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dodo.dodoserver.domain.category.entity.QCategory.category;
import static com.dodo.dodoserver.domain.nest.entity.QNest.nest;
import static com.dodo.dodoserver.domain.nest.entity.QNestCategory.nestCategory;

@Repository
@RequiredArgsConstructor
public class CategoryAdminRepositoryImpl implements CategoryAdminRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AdminCategoryResponseDto> findAllAdminCategories(boolean includeDeleted, String sortBy) {
        NumberExpression<Long> nestCount = Expressions.asNumber(
                JPAExpressions.select(nestCategory.count())
                        .from(nestCategory)
                        .join(nestCategory.nest, nest)
                        .where(nestCategory.category.id.eq(category.id)
                                .and(nest.deletedAt.isNull()))
        ).coalesce(0L);

        JPAQuery<AdminCategoryResponseDto> query = queryFactory
                .select(Projections.fields(AdminCategoryResponseDto.class,
                        category.id,
                        category.name,
                        category.sortOrder,
                        category.createdAt,
                        category.deletedAt,
                        nestCount.as("nestCount")
                ))
                .from(category);

        if (!includeDeleted) {
            query.where(category.deletedAt.isNull());
        }

        if ("nestCount".equals(sortBy)) {
            query.orderBy(nestCount.desc(), category.sortOrder.asc());
        } else {
            query.orderBy(category.sortOrder.asc(), category.createdAt.desc());
        }

        return query.fetch();
    }
}

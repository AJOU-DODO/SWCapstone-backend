package com.dodo.dodoserver.domain.admin.nest.dao;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dodo.dodoserver.domain.nest.entity.QNest.nest;
import static com.dodo.dodoserver.domain.nest.entity.QNestComment.nestComment;
import static com.dodo.dodoserver.domain.nest.entity.QNestReaction.nestReaction;
import static com.dodo.dodoserver.domain.report.entity.QReport.report;
import static com.dodo.dodoserver.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminNestRepositoryCustomImpl implements AdminNestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminNestResponseDto> findNestsForAdmin(
            Pageable pageable, 
            LocalDate startDate, 
            LocalDate endDate, 
            String sort, 
            String includeDeleted) {

        // 1. 기본 필터 조건 설정
        BooleanExpression dateCondition = null;
        if (startDate != null && endDate != null) {
            dateCondition = nest.createdAt.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }

        BooleanExpression deletedCondition = getDeletedCondition(includeDeleted);

        // 2. 메인 쿼리: Nest 정보와 각종 카운트 집계
        // @SQLRestriction 우회를 위해 selectFrom(nest) 대신 필드를 직접 선택
        List<Tuple> results = queryFactory
                .select(
                        nest.id,
                        user.nickname,
                        nest.content,
                        nest.createdAt,
                        nestReaction.countDistinct(),
                        nestComment.countDistinct(),
                        report.countDistinct(),
                        nest.viewCount
                )
                .from(nest)
                .join(nest.creator, user)
                .leftJoin(nestReaction).on(nestReaction.nest.eq(nest))
                .leftJoin(nestComment).on(nestComment.nest.eq(nest))
                .leftJoin(report).on(report.targetId.eq(nest.id).and(report.reportType.eq(ReportType.NEST)))
                .where(dateCondition, deletedCondition)
                .groupBy(nest.id, user.nickname, nest.content, nest.createdAt, nest.viewCount)
                .orderBy(getOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(nest.id.count())
                .from(nest)
                .where(dateCondition, deletedCondition)
                .fetchOne();

        if (results.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total != null ? total : 0L);
        }

        List<Long> nestIds = results.stream()
                .map(t -> t.get(nest.id))
                .collect(Collectors.toList());

        // 3. 신고 사유 리스트 별도 조회 (N+1 방지)
        Map<Long, List<ReportReason>> reasonMap = queryFactory
                .select(report.targetId, report.reason)
                .from(report)
                .where(report.targetId.in(nestIds), report.reportType.eq(ReportType.NEST))
                .fetch()
                .stream()
                .filter(t -> t.get(report.targetId) != null && t.get(report.reason) != null)
                .collect(Collectors.groupingBy(
                        t -> t.get(report.targetId),
                        Collectors.mapping(t -> t.get(report.reason), Collectors.toList())
                ));

        List<AdminNestResponseDto> content = results.stream().map(t -> {
            Long likeCount = t.get(nestReaction.countDistinct());
            Long commentCount = t.get(nestComment.countDistinct());
            Long reportCount = t.get(report.countDistinct());

            return AdminNestResponseDto.builder()
                    .nestId(t.get(nest.id))
                    .authorNickname(t.get(user.nickname))
                    .content(t.get(nest.content))
                    .createdAt(t.get(nest.createdAt))
                    .likeCount(likeCount != null ? likeCount : 0L)
                    .commentCount(commentCount != null ? commentCount : 0L)
                    .reportCount(reportCount != null ? reportCount : 0L)
                    .reasons(reasonMap.getOrDefault(t.get(nest.id), Collections.emptyList()).stream().distinct().toList())
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression getDeletedCondition(String includeDeleted) {
        if ("ALL".equalsIgnoreCase(includeDeleted)) {
            return null;
        } else if ("DELETED_ONLY".equalsIgnoreCase(includeDeleted)) {
            return nest.deletedAt.isNotNull();
        } else { // ACTIVE_ONLY 또는 기본값
            return nest.deletedAt.isNull();
        }
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (sort == null) return new OrderSpecifier<>(Order.DESC, nest.createdAt);
        return switch (sort.toUpperCase()) {
            case "LIKE" -> new OrderSpecifier<>(Order.DESC, nestReaction.countDistinct());
            case "COMMENT" -> new OrderSpecifier<>(Order.DESC, nestComment.countDistinct());
            case "VIEW" -> new OrderSpecifier<>(Order.DESC, nest.viewCount);
            default -> new OrderSpecifier<>(Order.DESC, nest.createdAt);
        };
    }
}

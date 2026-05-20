package com.dodo.dodoserver.domain.admin.nest.dao;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.nest.entity.Nest;
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

        // 2. 메인 쿼리: Nest 엔티티 조회 (정렬 및 필터링 적용)
        List<Nest> results = queryFactory
                .selectFrom(nest)
                .join(nest.creator, user).fetchJoin()
                .leftJoin(nestReaction).on(nestReaction.nest.eq(nest))
                .leftJoin(nestComment).on(nestComment.nest.eq(nest))
                .leftJoin(report).on(report.targetId.eq(nest.id).and(report.reportType.eq(ReportType.NEST)))
                .where(dateCondition, deletedCondition)
                .groupBy(nest.id)
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
                .map(Nest::getId)
                .collect(Collectors.toList());

        // 3. 통계 데이터 일괄 조회 (N+1 방지)
        Map<Long, Long> likeCountMap = queryFactory
                .select(nestReaction.nest.id, nestReaction.count())
                .from(nestReaction)
                .where(nestReaction.nest.id.in(nestIds))
                .groupBy(nestReaction.nest.id)
                .fetch().stream()
                .collect(Collectors.toMap(t -> t.get(nestReaction.nest.id), t -> t.get(nestReaction.count())));

        Map<Long, Long> commentCountMap = queryFactory
                .select(nestComment.nest.id, nestComment.count())
                .from(nestComment)
                .where(nestComment.nest.id.in(nestIds))
                .groupBy(nestComment.nest.id)
                .fetch().stream()
                .collect(Collectors.toMap(t -> t.get(nestComment.nest.id), t -> t.get(nestComment.count())));

        Map<Long, Long> reportCountMap = queryFactory
                .select(report.targetId, report.count())
                .from(report)
                .where(report.targetId.in(nestIds), report.reportType.eq(ReportType.NEST))
                .groupBy(report.targetId)
                .fetch().stream()
                .collect(Collectors.toMap(t -> t.get(report.targetId), t -> t.get(report.count())));

        // 4. 신고 사유 리스트 별도 조회
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

        List<AdminNestResponseDto> content = results.stream().map(n -> {
            return AdminNestResponseDto.builder()
                    .nestId(n.getId())
                    .authorNickname(n.getCreator().getNickname())
                    .content(n.getContent())
                    .createdAt(n.getCreatedAt())
                    .likeCount(likeCountMap.getOrDefault(n.getId(), 0L))
                    .commentCount(commentCountMap.getOrDefault(n.getId(), 0L))
                    .reportCount(reportCountMap.getOrDefault(n.getId(), 0L))
                    .isDeleted(n.getDeletedAt() != null)
                    .reasons(reasonMap.getOrDefault(n.getId(), Collections.emptyList()).stream().distinct().toList())
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

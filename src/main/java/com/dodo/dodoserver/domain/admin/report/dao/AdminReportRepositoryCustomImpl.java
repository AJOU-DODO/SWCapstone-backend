package com.dodo.dodoserver.domain.admin.report.dao;

import com.dodo.dodoserver.domain.admin.report.dto.AdminCommentReportResponseDto;
import com.dodo.dodoserver.domain.admin.report.dto.AdminNestReportResponseDto;
import com.dodo.dodoserver.domain.report.entity.ReportReason;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dodo.dodoserver.domain.nest.entity.QNest.nest;
import static com.dodo.dodoserver.domain.nest.entity.QNestComment.nestComment;
import static com.dodo.dodoserver.domain.report.entity.QReport.report;
import static com.dodo.dodoserver.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class AdminReportRepositoryCustomImpl implements AdminReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminNestReportResponseDto> findReportedNests(Pageable pageable, String sort) {
        // 1. 신고된 Nest ID 목록 및 통계 정보 조회 (PENDING, PROCESSED 상태만)
        List<Tuple> results = queryFactory
                .select(
                        report.targetId,
                        report.createdAt.min(),
                        report.createdAt.max(),
                        report.targetId.count(),
                        report.status.min() // 상태 정렬용 (PENDING < PROCESSED)
                )
                .from(report)
                .where(
                        report.reportType.eq(ReportType.NEST),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .groupBy(report.targetId)
                .orderBy(getNestOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(report.targetId.countDistinct())
                .from(report)
                .where(
                        report.reportType.eq(ReportType.NEST),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .fetchOne();

        if (results.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total != null ? total : 0L);
        }

        List<Long> targetIds = results.stream()
                .map(t -> t.get(report.targetId))
                .collect(Collectors.toList());

        // 2. Nest 상세 정보 및 모든 사유 조회
        Map<Long, List<ReportReason>> reasonMap = queryFactory
                .select(report.targetId, report.reason)
                .from(report)
                .where(report.targetId.in(targetIds), report.reportType.eq(ReportType.NEST))
                .fetch()
                .stream()
                .filter(t -> t.get(report.targetId) != null && t.get(report.reason) != null)
                .collect(Collectors.groupingBy(
                        t -> t.get(report.targetId),
                        Collectors.mapping(t -> t.get(report.reason), Collectors.toList())
                ));

        Map<Long, Tuple> nestInfoMap = queryFactory
                .select(nest.id, user.nickname, nest.content, report.status)
                .from(nest)
                .join(nest.creator, user)
                .join(report).on(report.targetId.eq(nest.id).and(report.reportType.eq(ReportType.NEST)))
                .where(nest.id.in(targetIds))
                .groupBy(nest.id, user.nickname, nest.content, report.status)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(nest.id), t -> t, (oldV, newV) -> oldV));

        List<AdminNestReportResponseDto> content = results.stream().map(t -> {
            Long id = t.get(report.targetId);
            Tuple info = nestInfoMap.get(id);
            Long reportCount = t.get(report.targetId.count());
            
            return AdminNestReportResponseDto.builder()
                    .nestId(id)
                    .authorNickname(info != null ? info.get(user.nickname) : "알 수 없음")
                    .content(info != null ? info.get(nest.content) : "")
                    .firstReportedAt(t.get(report.createdAt.min()))
                    .lastReportedAt(t.get(report.createdAt.max()))
                    .reportCount(reportCount != null ? reportCount : 0L)
                    .reasons(reasonMap.getOrDefault(id, Collections.emptyList()).stream().distinct().toList())
                    .status(info != null ? info.get(report.status) : ReportStatus.PENDING)
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<AdminCommentReportResponseDto> findReportedComments(Pageable pageable, String sort) {
        // Comment 신고 목록 조회 로직 (Nest와 유사)
        List<Tuple> results = queryFactory
                .select(
                        report.targetId,
                        report.createdAt.max(),
                        report.targetId.count(),
                        report.status.min()
                )
                .from(report)
                .where(
                        report.reportType.eq(ReportType.COMMENT),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .groupBy(report.targetId)
                .orderBy(getCommentOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(report.targetId.countDistinct())
                .from(report)
                .where(
                        report.reportType.eq(ReportType.COMMENT),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .fetchOne();

        if (results.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total != null ? total : 0L);
        }

        List<Long> targetIds = results.stream()
                .map(t -> t.get(report.targetId))
                .collect(Collectors.toList());

        Map<Long, List<ReportReason>> reasonMap = queryFactory
                .select(report.targetId, report.reason)
                .from(report)
                .where(
                        report.targetId.in(targetIds),
                        report.reportType.eq(ReportType.COMMENT),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .fetch()
                .stream()
                .filter(t -> t.get(report.targetId) != null && t.get(report.reason) != null)
                .collect(Collectors.groupingBy(
                        t -> t.get(report.targetId),
                        Collectors.mapping(t -> t.get(report.reason), Collectors.toList())
                ));

        Map<Long, Tuple> commentInfoMap = queryFactory
                .select(nestComment.id, user.nickname, nestComment.content, nest.id, nest.title, report.status)
                .from(nestComment)
                .join(nestComment.user, user)
                .join(nestComment.nest, nest)
                .join(report).on(report.targetId.eq(nestComment.id).and(report.reportType.eq(ReportType.COMMENT)))
                .where(
                        nestComment.id.in(targetIds),
                        report.status.ne(ReportStatus.REJECTED)
                )
                .groupBy(nestComment.id, user.nickname, nestComment.content, nest.id, nest.title, report.status)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(nestComment.id), t -> t, (oldV, newV) -> oldV));

        List<AdminCommentReportResponseDto> content = results.stream().map(t -> {
            Long id = t.get(report.targetId);
            Tuple info = commentInfoMap.get(id);
            Long reportCount = t.get(report.targetId.count());

            return AdminCommentReportResponseDto.builder()
                    .commentId(id)
                    .authorNickname(info != null ? info.get(user.nickname) : "알 수 없음")
                    .commentContent(info != null ? info.get(nestComment.content) : "")
                    .nestId(info != null ? info.get(nest.id) : null)
                    .nestTitle(info != null ? info.get(nest.title) : "알 수 없음")
                    .lastReportedAt(t.get(report.createdAt.max()))
                    .reportCount(reportCount != null ? reportCount : 0L)
                    .reasons(reasonMap.getOrDefault(id, Collections.emptyList()).stream().distinct().toList())
                    .status(info != null ? info.get(report.status) : ReportStatus.PENDING)
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Map<String, Long> countPendingReportsByTarget(ReportType targetType, Long targetId) {
        List<Tuple> results = queryFactory
                .select(report.reason, report.count())
                .from(report)
                .where(
                        report.reportType.eq(targetType),
                        report.targetId.eq(targetId),
                        report.status.eq(ReportStatus.PENDING)
                )
                .groupBy(report.reason)
                .fetch();

        Map<String, Long> stats = new HashMap<>();
        // 초기값 설정
        stats.put("pendingAbuseCount", 0L);
        stats.put("pendingSpamCount", 0L);
        stats.put("pendingAdvertisementCount", 0L);
        stats.put("pendingOtherCount", 0L);

        for (Tuple t : results) {
            ReportReason reason = t.get(report.reason);
            Long count = t.get(report.count());
            if (reason != null) {
                switch (reason) {
                    case ABUSE -> stats.put("pendingAbuseCount", count);
                    case SPAM -> stats.put("pendingSpamCount", count);
                    case ADVERTISEMENT -> stats.put("pendingAdvertisementCount", count);
                    case OTHER -> stats.put("pendingOtherCount", count);
                }
            }
        }
        return stats;
    }

    @Override
    public List<String> findOtherReportContents(ReportType targetType, Long targetId) {
        return queryFactory
                .select(report.content)
                .from(report)
                .where(
                        report.reportType.eq(targetType),
                        report.targetId.eq(targetId),
                        report.reason.eq(ReportReason.OTHER),
                        report.status.eq(ReportStatus.PENDING)
                )
                .fetch();
    }

    private OrderSpecifier<?> getNestOrderSpecifier(String sort) {
        if (sort == null) return new OrderSpecifier<>(Order.DESC, report.createdAt.max());
        return switch (sort) {
            case "FIRST_REPORT" -> new OrderSpecifier<>(Order.ASC, report.createdAt.min());
            case "REPORT_COUNT" -> new OrderSpecifier<>(Order.DESC, report.targetId.count());
            case "STATUS" -> new OrderSpecifier<>(Order.ASC, report.status.min());
            default -> new OrderSpecifier<>(Order.DESC, report.createdAt.max());
        };
    }

    private OrderSpecifier<?> getCommentOrderSpecifier(String sort) {
        if (sort == null) return new OrderSpecifier<>(Order.DESC, report.createdAt.max());
        return switch (sort) {
            case "REPORT_COUNT" -> new OrderSpecifier<>(Order.DESC, report.targetId.count());
            case "NEST_ID" -> new OrderSpecifier<>(Order.ASC, report.targetId); // 이 정렬은 조인이 필요할 수 있어 보완 필요
            default -> new OrderSpecifier<>(Order.DESC, report.createdAt.max());
        };
    }
}

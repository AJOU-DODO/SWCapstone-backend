package com.dodo.dodoserver.domain.admin.statistics.dao;

import com.dodo.dodoserver.domain.admin.statistics.dto.AdminSummaryResponseDto;
import com.dodo.dodoserver.domain.nest.entity.QNest;
import com.dodo.dodoserver.domain.nest.entity.QNestComment;
import com.dodo.dodoserver.domain.postcard.entity.QPostcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AdminStatisticsRepositoryImpl implements AdminStatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public AdminSummaryResponseDto getSummaryStats() {
        QNest nest = QNest.nest;
        QNestComment comment = QNestComment.nestComment;
        QPostcard postcard = QPostcard.postcard;

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Long totalNests = queryFactory
                .select(nest.count())
                .from(nest)
                .where(nest.deletedAt.isNull())
                .fetchOne();

        Long todayNests = queryFactory
                .select(nest.count())
                .from(nest)
                .where(nest.createdAt.goe(todayStart),
                        nest.deletedAt.isNull())
                .fetchOne();

        Long totalComments = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.deletedAt.isNull())
                .fetchOne();

        Long todayComments = queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.createdAt.goe(todayStart),
                        comment.deletedAt.isNull())
                .fetchOne();

        Long totalPostcards = queryFactory
                .select(postcard.count())
                .from(postcard)
                .where(postcard.isExchanged.isTrue(),
                        postcard.deletedAt.isNull())
                .fetchOne();

        Long todayPostcards = queryFactory
                .select(postcard.count())
                .from(postcard)
                .where(postcard.createdAt.goe(todayStart),
                        postcard.deletedAt.isNull())
                .fetchOne();

        return AdminSummaryResponseDto.builder()
                .totalNests(totalNests != null ? totalNests : 0L)
                .todayNests(todayNests != null ? todayNests : 0L)
                .totalComments(totalComments != null ? totalComments : 0L)
                .todayComments(todayComments != null ? todayComments : 0L)
                .totalPostcards(totalPostcards != null ? totalPostcards : 0L)
                .todayPostcards(todayPostcards != null ? todayPostcards : 0L)
                .build();
    }
}

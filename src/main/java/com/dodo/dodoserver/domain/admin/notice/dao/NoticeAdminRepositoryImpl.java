package com.dodo.dodoserver.domain.admin.notice.dao;

import com.dodo.dodoserver.domain.notice.entity.Notice;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.dodo.dodoserver.domain.notice.entity.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeAdminRepositoryImpl implements NoticeAdminRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notice> findAllNoticesWithDeleted(Pageable pageable, Boolean isPublished) {
        List<Notice> content = queryFactory
                .selectFrom(notice)
                .where(isPublishedEq(isPublished))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notice.createdAt.desc())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(notice.count())
                .from(notice)
                .where(isPublishedEq(isPublished))
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<Notice> findByIdWithDeleted(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(notice)
                .where(notice.id.eq(id))
                .fetchOne());
    }

    private com.querydsl.core.types.dsl.BooleanExpression isPublishedEq(Boolean isPublished) {
        return isPublished != null ? notice.isPublished.eq(isPublished) : null;
    }
}

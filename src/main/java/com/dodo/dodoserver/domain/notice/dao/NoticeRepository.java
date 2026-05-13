package com.dodo.dodoserver.domain.notice.dao;

import com.dodo.dodoserver.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 사용자용 레포지토리 - @SQLRestriction("deleted_at IS NULL") 자동 적용됨
    Optional<Notice> findByIdAndIsPublishedTrue(Long id);
    org.springframework.data.domain.Page<Notice> findAllByIsPublishedTrue(org.springframework.data.domain.Pageable pageable);
}

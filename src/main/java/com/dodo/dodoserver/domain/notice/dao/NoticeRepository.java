package com.dodo.dodoserver.domain.notice.dao;

import com.dodo.dodoserver.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 사용자용 레포지토리 - 명시적으로 삭제되지 않은(deleted_at IS NULL) 항목만 조회
    Optional<Notice> findByIdAndIsPublishedTrueAndDeletedAtIsNull(Long id);
    Page<Notice> findAllByIsPublishedTrueAndDeletedAtIsNull(Pageable pageable);
}

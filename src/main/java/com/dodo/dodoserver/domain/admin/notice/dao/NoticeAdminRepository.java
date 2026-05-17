package com.dodo.dodoserver.domain.admin.notice.dao;

import com.dodo.dodoserver.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeAdminRepository {
    // 관리자는 삭제된 공지사항도 조회 가능해야 하므로 Querydsl로 직접 구현
    Page<Notice> findAllNoticesWithDeleted(Pageable pageable);
    Optional<Notice> findByIdWithDeleted(Long id);
}

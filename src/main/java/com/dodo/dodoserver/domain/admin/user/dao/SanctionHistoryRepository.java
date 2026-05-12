package com.dodo.dodoserver.domain.admin.user.dao;

import com.dodo.dodoserver.domain.admin.user.entity.SanctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionHistoryRepository extends JpaRepository<SanctionHistory, Long> {
    List<SanctionHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}

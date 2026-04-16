package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.UnlockHistory;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnlockHistoryRepository extends JpaRepository<UnlockHistory, Long> {
    boolean existsByUserAndNest(User user, Nest nest);
    Optional<UnlockHistory> findByUserAndNest(User user, Nest nest);
}

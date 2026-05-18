package com.dodo.dodoserver.domain.admin.user.dao;

import com.dodo.dodoserver.domain.admin.user.entity.AdminEmailWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminEmailWhitelistRepository extends JpaRepository<AdminEmailWhitelist, Long> {
    Optional<AdminEmailWhitelist> findByEmail(String email);
    boolean existsByEmail(String email);
}

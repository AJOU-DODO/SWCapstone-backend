package com.dodo.dodoserver.domain.user.dao;

import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdvertiserAuthorityRepository extends JpaRepository<AdvertiserAuthority, Long> {
    Optional<AdvertiserAuthority> findByUser(User user);
    Optional<AdvertiserAuthority> findByUserId(Long userId);

    @Query("SELECT a FROM AdvertiserAuthority a JOIN FETCH a.user WHERE a.expiredAt < :now")
    List<AdvertiserAuthority> findAllByExpiredAtBefore(@Param("now") LocalDateTime now);
}

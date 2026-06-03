package com.dodo.dodoserver.domain.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NestAdInfoRepository extends JpaRepository<NestAdInfo, Long> {
    Optional<NestAdInfo> findByNest(Nest nest);
    Optional<NestAdInfo> findByNestId(Long nestId);

    List<NestAdInfo> findAllByExpiredAtBefore(LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NestAdInfo n SET n.impressions = n.impressions + 1 WHERE n.nest.id IN :nestIds")
    void incrementImpressions(@Param("nestIds") List<Long> nestIds);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NestAdInfo n SET n.clicks = n.clicks + 1 WHERE n.nest.id = :nestId")
    void incrementClicks(@Param("nestId") Long nestId);
}

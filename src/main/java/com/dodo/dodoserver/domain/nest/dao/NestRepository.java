package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.dao.querydsl.NestRepositoryCustom;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NestRepository extends JpaRepository<Nest, Long>, NestRepositoryCustom {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Nest n SET n.viewCount = n.viewCount + :increment WHERE n.id = :nestId")
    void incrementViewCount(@Param("nestId") Long nestId, @Param("increment") Long increment);
}

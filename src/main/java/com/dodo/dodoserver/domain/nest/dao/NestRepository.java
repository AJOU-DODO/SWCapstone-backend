package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.admin.nest.dao.AdminNestRepositoryCustom;
import com.dodo.dodoserver.domain.nest.dao.querydsl.NestRepositoryCustom;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NestRepository extends JpaRepository<Nest, Long>, NestRepositoryCustom, AdminNestRepositoryCustom {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Nest n SET n.viewCount = n.viewCount + :increment WHERE n.id = :nestId")
    void incrementViewCount(@Param("nestId") Long nestId, @Param("increment") Long increment);

    long countByCreatorAndIsAdTrueAndDeletedAtIsNull(User creator);

    List<Nest> findAllByCreatorAndIsAdTrueAndDeletedAtIsNull(User creator);

    List<Nest> findAllByCreatorAndIsAdTrue(User creator);
}

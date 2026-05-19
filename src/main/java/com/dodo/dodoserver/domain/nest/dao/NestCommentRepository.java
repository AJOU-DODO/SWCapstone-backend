package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NestCommentRepository extends JpaRepository<NestComment, Long> {
    // 특정 둥지의 모든 댓글 조회 (User 정보 FETCH JOIN)
    @Query("SELECT nc FROM NestComment nc JOIN FETCH nc.user WHERE nc.nest = :nest")
    List<NestComment> findAllByNestWithUser(@Param("nest") Nest nest);

    // [어드민/가림처리용] 삭제된 댓글을 포함하여 특정 둥지의 모든 댓글 조회 (Native Query로 SQLRestriction 우회)
    @Query(value = "SELECT * FROM nest_comments WHERE nest_id = :nestId", nativeQuery = true)
    List<NestComment> findAllByNestIdIncludingDeletedNative(@Param("nestId") Long nestId);
}

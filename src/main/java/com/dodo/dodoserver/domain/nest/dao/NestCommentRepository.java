package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NestCommentRepository extends JpaRepository<NestComment, Long> {
    // 둥지 내 최상위 댓글(parent IS NULL) 조회 (User 정보 FETCH JOIN)
    @Query("SELECT nc FROM NestComment nc JOIN FETCH nc.user WHERE nc.nest = :nest AND nc.parent IS NULL ORDER BY nc.createdAt ASC")
    List<NestComment> findAllByNestAndParentIsNullOrderByCreatedAtAsc(@Param("nest") Nest nest);

    @Query("SELECT nc FROM NestComment nc JOIN FETCH nc.user WHERE nc.nest = :nest AND nc.parent IS NULL ORDER BY nc.createdAt DESC")
    List<NestComment> findAllByNestAndParentIsNullOrderByCreatedAtDesc(@Param("nest") Nest nest);

    @Query("SELECT nc FROM NestComment nc JOIN FETCH nc.user WHERE nc.nest = :nest AND nc.parent IS NULL ORDER BY nc.likeCount DESC, nc.createdAt DESC")
    List<NestComment> findAllByNestAndParentIsNullOrderByLikeCountDescCreatedAtDesc(@Param("nest") Nest nest);
}

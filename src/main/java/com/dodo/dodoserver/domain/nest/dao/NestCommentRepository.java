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


}

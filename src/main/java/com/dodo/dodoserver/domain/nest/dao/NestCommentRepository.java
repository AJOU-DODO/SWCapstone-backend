package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NestCommentRepository extends JpaRepository<NestComment, Long> {
    // 둥지 내 최상위 댓글(parent IS NULL) 생성순 조회
    List<NestComment> findAllByNestAndParentIsNullOrderByCreatedAtAsc(Nest nest);
}

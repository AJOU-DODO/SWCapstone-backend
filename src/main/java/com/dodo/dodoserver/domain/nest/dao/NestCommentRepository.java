package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NestCommentRepository extends JpaRepository<NestComment, Long> {
    // 둥지에 달린 최상위 댓글(parent IS NULL)들을 생성순으로 조회
    List<NestComment> findAllByNestAndParentIsNullOrderByCreatedAtAsc(Nest nest);
}

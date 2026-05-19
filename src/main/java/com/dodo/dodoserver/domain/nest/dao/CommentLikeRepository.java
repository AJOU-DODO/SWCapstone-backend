package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.CommentLike;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, NestComment comment);
    boolean existsByUserAndComment(User user, NestComment comment);
    List<CommentLike> findAllByUserAndCommentIn(User user, Collection<NestComment> comments);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment = :comment")
    void deleteByComment(@Param("comment") NestComment comment);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment IN :comments")
    void deleteByCommentIn(@Param("comments") Collection<NestComment> comments);
}

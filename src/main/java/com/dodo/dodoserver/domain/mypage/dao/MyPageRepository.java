package com.dodo.dodoserver.domain.mypage.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyPageRepository {
    Page<Nest> findNestsByUserAndCategory(User user, Long categoryId, Pageable pageable);
    Page<NestComment> findCommentsByUser(User user, Pageable pageable);
    Page<NestDraft> findDraftsByUser(User user, Pageable pageable);
    Page<Nest> findUnlockedNestsByUser(User user, Pageable pageable);
    Page<NestComment> findCommentsOnUserNests(User user, Pageable pageable);
    Page<Nest> findLikedNestsByUser(User user, Pageable pageable);

    long countNestsByUser(User user);
    long countCommentsByUser(User user);
    long countPostcardsByUser(User user);
}

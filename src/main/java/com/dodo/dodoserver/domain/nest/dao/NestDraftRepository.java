package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NestDraftRepository extends JpaRepository<NestDraft, Long> {
    List<NestDraft> findAllByCreatorOrderByCreatedAtDesc(User creator);
}

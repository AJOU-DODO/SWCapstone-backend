package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestReaction;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NestReactionRepository extends JpaRepository<NestReaction, Long> {
    Optional<NestReaction> findByUserAndNest(User user, Nest nest);
    long countByNestAndReactionType(Nest nest, ReactionType reactionType);
}

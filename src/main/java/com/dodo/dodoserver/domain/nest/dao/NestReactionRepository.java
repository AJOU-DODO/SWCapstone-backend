package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestReaction;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NestReactionRepository extends JpaRepository<NestReaction, Long> {
    Optional<NestReaction> findByUserAndNest(User user, Nest nest);
    long countByNestAndReactionType(Nest nest, ReactionType reactionType);

    @Modifying
    @Query("DELETE FROM NestReaction nr WHERE nr.nest = :nest")
    void deleteByNest(@Param("nest") Nest nest);
}

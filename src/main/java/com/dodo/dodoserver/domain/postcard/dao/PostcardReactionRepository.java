package com.dodo.dodoserver.domain.postcard.dao;

import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReaction;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostcardReactionRepository extends JpaRepository<PostcardReaction, Long> {
    Optional<PostcardReaction> findByPostcardAndUser(Postcard postcard, User user);
}

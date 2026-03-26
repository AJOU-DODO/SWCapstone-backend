package com.dodo.dodoserver.repository;

import com.dodo.dodoserver.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByEmail(String email);
    void deleteByEmail(String email);
}

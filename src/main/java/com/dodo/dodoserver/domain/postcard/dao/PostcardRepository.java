package com.dodo.dodoserver.domain.postcard.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostcardRepository extends JpaRepository<Postcard, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Postcard p WHERE p.id = :id")
    Optional<Postcard> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Postcard p WHERE p.nest = :nest AND p.isShared = true")
    Optional<Postcard> findSharedPostcardByNestForUpdate(@Param("nest") Nest nest);

    @Query("SELECT p FROM Postcard p WHERE p.nest = :nest AND p.isShared = true")
    Optional<Postcard> findSharedPostcardByNest(@Param("nest") Nest nest);
}

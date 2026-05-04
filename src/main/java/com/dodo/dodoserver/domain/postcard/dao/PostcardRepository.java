package com.dodo.dodoserver.domain.postcard.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT p FROM Postcard p WHERE p.nest IN :nests AND p.isShared = true")
    List<Postcard> findAllByNestInAndIsSharedTrue(@Param("nests") java.util.Collection<Nest> nests);

    @Query("SELECT p FROM Postcard p WHERE p.originalAuthor = :user OR p.currentOwner = :user")
    List<Postcard> findInventoryByUser(@Param("user") User user, Sort sort);
}

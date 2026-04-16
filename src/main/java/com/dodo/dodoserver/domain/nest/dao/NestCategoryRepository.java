package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NestCategoryRepository extends JpaRepository<NestCategory, Long> {
    List<NestCategory> findAllByNest(Nest nest);
    void deleteByNest(Nest nest);
}

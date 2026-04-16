package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.NestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NestCategoryRepository extends JpaRepository<NestCategory, Long> {
}

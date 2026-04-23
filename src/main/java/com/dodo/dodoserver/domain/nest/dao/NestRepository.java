package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.dao.querydsl.NestRepositoryCustom;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NestRepository extends JpaRepository<Nest, Long>, NestRepositoryCustom {
}

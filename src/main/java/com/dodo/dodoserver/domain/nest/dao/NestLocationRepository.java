package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.entity.NestLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NestLocationRepository extends JpaRepository<NestLocation, Long> {
}

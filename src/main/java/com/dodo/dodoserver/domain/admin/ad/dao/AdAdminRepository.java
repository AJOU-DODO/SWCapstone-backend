package com.dodo.dodoserver.domain.admin.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdAdminRepository extends JpaRepository<NestAdInfo, Long>, AdAdminRepositoryCustom {
}

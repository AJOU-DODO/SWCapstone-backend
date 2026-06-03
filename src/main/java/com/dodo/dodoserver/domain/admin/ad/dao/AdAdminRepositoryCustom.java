package com.dodo.dodoserver.domain.admin.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.admin.ad.dto.AdStatusFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdAdminRepositoryCustom {
    Page<NestAdInfo> findAllWithFilter(AdStatusFilter filter, Pageable pageable);
}

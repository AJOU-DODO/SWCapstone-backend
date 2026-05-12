package com.dodo.dodoserver.domain.admin.user.dao;

import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAdminRepositoryCustom {
    Page<UserAdminResponseDto> findAllUserAdminInfo(Pageable pageable);
}

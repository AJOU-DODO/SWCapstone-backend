package com.dodo.dodoserver.domain.admin.dao;

import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAdminRepository extends JpaRepository<User, Long>, UserAdminRepositoryCustom {
}

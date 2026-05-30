package com.dodo.dodoserver.domain.inquiry.dao;

import com.dodo.dodoserver.domain.admin.inquiry.dao.AdminInquiryRepositoryCustom;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long>, AdminInquiryRepositoryCustom {
    Page<Inquiry> findAllByUser(User user, Pageable pageable);
}

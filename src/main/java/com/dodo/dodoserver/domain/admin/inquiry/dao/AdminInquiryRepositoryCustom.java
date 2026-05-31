package com.dodo.dodoserver.domain.admin.inquiry.dao;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminInquiryRepositoryCustom {
    Page<AdminInquiryResponseDto> findInquiriesForAdmin(InquiryStatus status, Pageable pageable);
}

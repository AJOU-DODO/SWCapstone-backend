package com.dodo.dodoserver.domain.admin.inquiry.service;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryDetailResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.InquiryAnswerRequestDto;
import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;

    public Page<AdminInquiryResponseDto> getInquiries(InquiryStatus status, Pageable pageable) {
        return inquiryRepository.findInquiriesForAdmin(status, pageable);
    }

    public AdminInquiryDetailResponseDto getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        return AdminInquiryDetailResponseDto.from(inquiry);
    }

    @Transactional
    public void answerInquiry(Long inquiryId, InquiryAnswerRequestDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        
        inquiry.addAnswer(requestDto.getAnswer());
        
        // TODO: FCM 발송 로직 (Phase 4에서 구현 예정)
    }
}

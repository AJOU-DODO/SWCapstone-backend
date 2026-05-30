package com.dodo.dodoserver.domain.admin.inquiry.service;

import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryDetailResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.AdminInquiryResponseDto;
import com.dodo.dodoserver.domain.admin.inquiry.dto.InquiryAnswerRequestDto;
import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.domain.inquiry.service.InquiryNotificationService;
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
    private final InquiryNotificationService inquiryNotificationService;

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
        
        // FCM 발송 (이벤트 발행 방식)
        inquiryNotificationService.sendInquiryAnswerNotification(inquiry);
    }
}

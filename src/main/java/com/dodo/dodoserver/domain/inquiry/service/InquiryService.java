package com.dodo.dodoserver.domain.inquiry.service;

import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryRequestDto;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.user.entity.User;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public InquiryResponseDto createInquiry(User user, InquiryRequestDto requestDto) {
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .type(requestDto.getType())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        return InquiryResponseDto.from(inquiryRepository.save(inquiry));
    }

    public Page<InquiryResponseDto> getMyInquiries(User user, Pageable pageable) {
        return inquiryRepository.findAllByUser(user, pageable)
                .map(InquiryResponseDto::from);
    }

    @Transactional
    public InquiryResponseDto updateInquiry(User user, Long inquiryId, InquiryRequestDto requestDto) {
        Inquiry inquiry = findById(inquiryId);
        inquiry.validateOwner(user);
        inquiry.update(requestDto.getType(), requestDto.getTitle(), requestDto.getContent());

        return InquiryResponseDto.from(inquiry);
    }

    @Transactional
    public void deleteInquiry(User user, Long inquiryId) {
        Inquiry inquiry = findById(inquiryId);
        inquiry.validateOwner(user);
        inquiry.validateForDelete();

        inquiryRepository.delete(inquiry);
    }

    private Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }
}
